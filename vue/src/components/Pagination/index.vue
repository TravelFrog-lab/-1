<template>
    <div class="pagination-wrapper">
      <el-pagination
        :background="background"
        :current-page.sync="currentPage"
        :page-sizes="pageSizes"
        :page-size.sync="pageSize"
        :total="total"
        :layout="layout"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </template>
  
  <script>
  export default {
    name: 'Pagination',
    props: {
      page: {
        type: Number,
        default: 1,
        required: true
      },
      limit: {
        type: Number,
        default: 10,
        required: true
      },
      background: {
        type: Boolean,
        default: true
      },
      pageSizes: {
        type: Array,
        default: () => [10, 15, 20, 50]
      },
      total: {
        type: Number,
        default: 0,
        required: true
      },
      layout: {
        type: String,
        default: 'total, prev, pager, next, sizes, jumper'
      }
    },
    computed: {
      currentPage: {
        get() {
          return this.page
        },
        set(val) {
          this.$emit('update:page', val)
        }
      },
      pageSize: {
        get() {
          return this.limit
        },
        set(val) {
          this.$emit('update:limit', val)
        }
      }
    },
    methods: {
      handleSizeChange(val) {
        this.$emit('pagination', { page: this.currentPage, limit: val })
      },
      handleCurrentChange(val) {
        this.$emit('pagination', { page: val, limit: this.pageSize })
      }
    }
  }
  </script>
  
  <style lang="less">
  .pagination-wrapper {
    padding: 24px 0;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    border-top: 1px solid #ebeef5;

    .el-pagination {
      padding: 0;
      font-weight: normal;

      .btn-prev,
      .btn-next {
        background-color: #fff;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        padding: 0 12px;
        height: 32px;
        line-height: 32px;
        margin: 0 4px;

        &:hover {
          color: #409eff;
          border-color: #c6e2ff;
          background-color: #ecf5ff;
        }
      }

      .el-pager {
        li {
          background-color: #fff;
          border: 1px solid #dcdfe6;
          border-radius: 4px;
          padding: 0 12px;
          height: 32px;
          line-height: 32px;
          margin: 0 4px;
          min-width: 32px;

          &:hover {
            color: #409eff;
            border-color: #c6e2ff;
            background-color: #ecf5ff;
          }

          &.active {
            background-color: #409eff;
            color: #fff;
            border-color: #409eff;

            &:hover {
              background-color: #66b1ff;
            }
          }
        }
      }

      .el-pagination__sizes {
        margin: 0 16px;

        .el-input {
          .el-input__inner {
            height: 32px;
            line-height: 32px;
            border-radius: 4px;
          }
        }
      }

      .el-pagination__jump {
        margin-left: 16px;

        .el-input {
          width: 70px;
          margin: 0 8px;

          .el-input__inner {
            height: 32px;
            line-height: 32px;
            border-radius: 4px;
            padding: 0 8px;
          }
        }
      }
    }
  }
  </style>
  