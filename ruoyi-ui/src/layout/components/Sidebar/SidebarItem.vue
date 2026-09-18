<template>
  <div v-if="!item.hidden">
    <template
      v-if="hasOneShowingChild(item.children, item) && (!onlyOneChild.children || onlyOneChild.noShowingChildren)"
    >
      <app-link :to="resolvePath(onlyOneChild.path)">
        <el-menu-item :index="resolvePath(onlyOneChild.path)">
          <item
            :icon="onlyOneChild.meta && onlyOneChild.meta.icon"
            :title="onlyOneChild.meta && onlyOneChild.meta.title"
          />
        </el-menu-item>
      </app-link>
    </template>

    <el-submenu v-else :index="resolvePath(item.path)">
      <template slot="title">
        <item v-if="item.meta" :icon="item.meta.icon" :title="item.meta.title" />
      </template>
      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(child.path)"
      />
    </el-submenu>
  </div>
</template>

<script>
import Item from './Item'

export default {
  name: 'SidebarItem',
  components: { Item },
  props: {
    item: { type: Object, required: true },
    basePath: { type: String, default: '' }
  },
  data() {
    return {
      onlyOneChild: null
    }
  },
  methods: {
    hasOneShowingChild(children = [], parent) {
      const showingChildren = children.filter(c => !c.hidden)
      if (showingChildren.length === 1) {
        this.onlyOneChild = showingChildren[0]
        return true
      }
      if (showingChildren.length === 0) {
        this.onlyOneChild = { ...parent, path: '', noShowingChildren: true }
        return true
      }
      return false
    },
    resolvePath(routePath) {
      if (routePath === undefined || routePath === null || routePath === '') {
        return this.basePath
      }
      if (/^(https?:|mailto:|tel:)/.test(routePath)) {
        return routePath
      }
      if (routePath.startsWith('/')) {
        return routePath
      }
      const base = this.basePath.replace(/\/+$/, '')
      return base + '/' + routePath
    }
  }
}
</script>
