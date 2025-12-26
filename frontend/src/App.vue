<template>
  <div class="app">
    <header class="header">
      <div>
        <h1>Smart Image Processing Platform</h1>
        <p class="subtitle">Vue 3 前端演示，连接 Spring Boot REST API</p>
      </div>
      <div class="user-status">
        <template v-if="user">
          <div class="chip">登录用户：{{ user.username }} ({{ user.role }})</div>
          <button class="secondary" @click="logout">退出</button>
        </template>
      </div>
    </header>

    <main class="grid">
      <section class="card">
        <h2>账号登录 / 注册</h2>
        <form @submit.prevent="handleLogin">
          <label>
            用户名
            <input v-model="authForm.username" placeholder="student_a" required />
          </label>
          <label>
            密码
            <input v-model="authForm.password" type="password" placeholder="password123" required />
          </label>
          <div class="row">
            <button type="submit">登录</button>
            <button type="button" class="secondary" @click="handleRegister">注册</button>
          </div>
        </form>
        <p class="hint">提示：可使用预设账号 student_a / student_b / student_c</p>
      </section>

      <section class="card">
        <h2>图片上传与管理</h2>
        <form @submit.prevent="handleUpload">
          <label>
            选择图片 (JPG/PNG, &lt;10MB)
            <input type="file" accept="image/png, image/jpeg" @change="handleFileChange" />
          </label>
          <button type="submit" :disabled="!selectedFile || !user">上传</button>
        </form>
        <div class="row between">
          <h3>我的图片</h3>
          <button class="secondary" @click="fetchImages" :disabled="!user">刷新</button>
        </div>
        <div v-if="images.length === 0" class="hint">暂无图片，请先上传。</div>
        <ul v-else class="list">
          <li v-for="image in images" :key="image.id">
            <div>
              <strong>{{ image.filename }}</strong>
              <div class="meta">{{ image.contentType }} · {{ formatSize(image.size) }}</div>
            </div>
            <button class="ghost" @click="downloadImage(image.id)">下载</button>
          </li>
        </ul>
      </section>

      <section class="card">
        <h2>AI 功能演示</h2>
        <label>
          选择图片
          <select v-model="selectedImageId">
            <option disabled value="">请选择</option>
            <option v-for="image in images" :key="image.id" :value="image.id">
              {{ image.filename }}
            </option>
          </select>
        </label>
        <label>
          风格 / 模板 / 格式
          <input v-model="taskOption" placeholder="例如：van-gogh / studio / png" />
        </label>
        <div class="row wrap">
          <button @click="runTask('enhance')" :disabled="!canRunTask">一键增强</button>
          <button @click="runTask('style-transfer')" :disabled="!canRunTask">风格迁移</button>
          <button @click="runTask('background-replace')" :disabled="!canRunTask">背景替换</button>
          <button @click="runTask('compress')" :disabled="!canRunTask">压缩</button>
          <button @click="runTask('convert')" :disabled="!canRunTask">格式转换</button>
        </div>
        <div class="hint">AI 接口为示例调用，服务不可用时会返回错误信息。</div>
      </section>

      <section class="card">
        <h2>任务历史记录</h2>
        <div class="row between">
          <span>任务总数：{{ tasks.length }}</span>
          <button class="secondary" @click="fetchTasks" :disabled="!user">刷新</button>
        </div>
        <div v-if="tasks.length === 0" class="hint">暂无任务记录。</div>
        <ul v-else class="list">
          <li v-for="task in tasks" :key="task.taskId">
            <div>
              <strong>{{ task.taskType }}</strong>
              <div class="meta">状态：{{ task.status }} · {{ formatDate(task.createdAt) }}</div>
              <div class="meta">{{ task.message }}</div>
            </div>
          </li>
        </ul>
      </section>

      <section class="card">
        <h2>批处理示例</h2>
        <p class="hint">基于当前用户与已上传图片演示批处理任务。</p>
        <button @click="runBatch" :disabled="!canRunTask || images.length < 2">提交批处理</button>
      </section>
    </main>

    <section class="card log">
      <h2>操作日志</h2>
      <div v-if="logs.length === 0" class="hint">暂无日志。</div>
      <ul v-else class="list">
        <li v-for="(log, index) in logs" :key="index">
          {{ log }}
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue';

const authForm = reactive({ username: '', password: '' });
const user = ref(null);
const images = ref([]);
const tasks = ref([]);
const selectedFile = ref(null);
const selectedImageId = ref('');
const taskOption = ref('');
const logs = ref([]);

const canRunTask = computed(() => user.value && selectedImageId.value);

const log = (message) => {
  logs.value.unshift(`${new Date().toLocaleTimeString()} - ${message}`);
};

const handleFileChange = (event) => {
  selectedFile.value = event.target.files[0] || null;
};

const handleLogin = async () => {
  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(authForm)
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || '登录失败');
    }
    user.value = data;
    log(`登录成功：${data.username}`);
    await fetchImages();
    await fetchTasks();
  } catch (error) {
    log(`登录失败：${error.message}`);
  }
};

const handleRegister = async () => {
  try {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(authForm)
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || '注册失败');
    }
    user.value = data;
    log(`注册成功：${data.username}`);
  } catch (error) {
    log(`注册失败：${error.message}`);
  }
};

const logout = () => {
  user.value = null;
  images.value = [];
  tasks.value = [];
  selectedImageId.value = '';
  log('已退出登录');
};

const handleUpload = async () => {
  if (!user.value || !selectedFile.value) {
    return;
  }
  try {
    const formData = new FormData();
    formData.append('file', selectedFile.value);
    const response = await fetch(`/api/images/upload?userId=${user.value.userId}`, {
      method: 'POST',
      body: formData
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || data || '上传失败');
    }
    log(`上传成功：${data.filename}`);
    selectedFile.value = null;
    await fetchImages();
  } catch (error) {
    log(`上传失败：${error.message}`);
  }
};

const fetchImages = async () => {
  if (!user.value) return;
  try {
    const response = await fetch(`/api/images?userId=${user.value.userId}`);
    const data = await response.json();
    images.value = Array.isArray(data) ? data : [];
    if (images.value.length > 0 && !selectedImageId.value) {
      selectedImageId.value = images.value[0].id;
    }
  } catch (error) {
    log(`获取图片失败：${error.message}`);
  }
};

const downloadImage = (imageId) => {
  window.open(`/api/images/${imageId}/download`, '_blank');
};

const runTask = async (type) => {
  if (!user.value || !selectedImageId.value) {
    return;
  }
  const option = encodeURIComponent(taskOption.value || '');
  let url = '';
  if (type === 'enhance') {
    url = `/api/ai/enhance?userId=${user.value.userId}&imageId=${selectedImageId.value}`;
  } else if (type === 'style-transfer') {
    url = `/api/ai/style-transfer?userId=${user.value.userId}&imageId=${selectedImageId.value}&style=${option || 'van-gogh'}`;
  } else if (type === 'background-replace') {
    url = `/api/ai/background-replace?userId=${user.value.userId}&imageId=${selectedImageId.value}&template=${option || 'studio'}`;
  } else if (type === 'compress') {
    url = `/api/tasks/compress?userId=${user.value.userId}&imageId=${selectedImageId.value}`;
  } else if (type === 'convert') {
    url = `/api/tasks/convert?userId=${user.value.userId}&imageId=${selectedImageId.value}&format=${option || 'png'}`;
  }
  try {
    const response = await fetch(url, { method: 'POST' });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || data || '任务提交失败');
    }
    log(`任务提交成功：${data.taskType}`);
    await fetchTasks();
  } catch (error) {
    log(`任务提交失败：${error.message}`);
  }
};

const runBatch = async () => {
  if (!user.value || images.value.length < 2) {
    return;
  }
  const payload = {
    userId: user.value.userId,
    tasks: [
      { imageId: images.value[0].id, taskType: 'ENHANCE' },
      { imageId: images.value[1].id, taskType: 'CONVERT', option: 'jpg' }
    ]
  };
  try {
    const response = await fetch('/api/tasks/batch', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || '批处理失败');
    }
    log(`批处理提交成功：${data.length} 个任务`);
    await fetchTasks();
  } catch (error) {
    log(`批处理失败：${error.message}`);
  }
};

const fetchTasks = async () => {
  if (!user.value) return;
  try {
    const response = await fetch(`/api/tasks/history?userId=${user.value.userId}`);
    const data = await response.json();
    tasks.value = Array.isArray(data) ? data : [];
  } catch (error) {
    log(`获取任务失败：${error.message}`);
  }
};

const formatSize = (size) => {
  if (!size) return '0 B';
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / (1024 * 1024)).toFixed(2)} MB`;
};

const formatDate = (value) => {
  if (!value) return '-';
  return new Date(value).toLocaleString();
};
</script>

<style scoped>
:root {
  color-scheme: light;
}

.app {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #1f2933;
  padding: 32px;
  background: #f6f8fb;
  min-height: 100vh;
}

.header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  gap: 16px;
}

.subtitle {
  color: #52606d;
  margin-top: 4px;
}

.user-status {
  display: flex;
  gap: 12px;
  align-items: center;
}

.chip {
  background: #e1e7ff;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.9rem;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.card {
  background: #ffffff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.08);
}

.card h2 {
  margin-top: 0;
  margin-bottom: 16px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #334e68;
}

input,
select {
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid #cbd2d9;
}

button {
  background: #2563eb;
  color: #fff;
  border: none;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

button.secondary {
  background: #5f6c80;
}

button.ghost {
  background: transparent;
  color: #2563eb;
  padding: 6px 10px;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.row.between {
  justify-content: space-between;
}

.row.wrap {
  flex-wrap: wrap;
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7eb;
}

.meta {
  font-size: 0.85rem;
  color: #7b8794;
}

.hint {
  font-size: 0.85rem;
  color: #7b8794;
}

.log {
  margin-top: 24px;
}
</style>
