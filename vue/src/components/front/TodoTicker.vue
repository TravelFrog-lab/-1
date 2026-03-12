<template>
  <div class="todo-ticker" v-if="isLoggedIn && items && items.length">
    <el-scrollbar class="ticker-scroll" :wrap-style="{ width: '100%' }">
      <div class="ticker-items">
        <span class="ticker-item" v-for="item in items" :key="item.id">
          <i :class="item.icon" class="item-icon"></i>
          {{ item.title }}
        </span>
      </div>
    </el-scrollbar>
  </div>
</template>

<script>
export default {
  name: 'TodoTicker',
  props: {
    items: {
      type: Array,
      default: () => []
    },
    isLoggedIn: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style scoped>
.todo-ticker {
  margin: 20px 0;
  background-color: #fff;
  padding: 10px;
  border-radius: 6px;
}
.ticker-items {
  display: flex;
  align-items: center;
  white-space: nowrap;
  animation: scroll 15s linear infinite;
}
.ticker-item {
  margin-right: 40px;
  font-size: 14px;
}
@keyframes scroll {
  0% { transform: translateX(0); }
  100% { transform: translateX(-100%); }
}
</style>