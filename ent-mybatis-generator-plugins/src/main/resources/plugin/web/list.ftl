<template>
  <div>
    <EntTable
      :search-info="searchInfo"
      :scroll-x="1600"
      :row-selection="{ type: 'checkbox', selectedRowKeys: checkedKeys, onChange: onSelectChange }"
      @register="registerTable"
    >
      <template #toolbar>
        <NPopconfirm v-if="hasPermission('${model.camelName}:batch-delete')" @positive-click="handleBatchDelete">
          <template #trigger>
            <ent-button type="primary" danger :disabled="checkedKeys.length === 0">删除</ent-button>
          </template>
          确认删除所选记录?
        </NPopconfirm>
        <ent-button v-if="hasPermission('${model.camelName}:create')" type="primary" @click="handleCreate">新增${model.description}</ent-button>
      </template>
    </EntTable>
    <${model.name}EditDrawer @register="registerEditDrawer" @success="handleEditSuccess" />
    <${model.name}DetailDrawer @register="registerDetailDrawer" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, h, reactive, ref } from 'vue';
  import { EntTable, EntTableAction, useTable } from 'fe-ent-core/es/components/table';
  import { ${model.name}BatchDelete, ${model.name}Delete, ${model.name}Page } from '${projectRootAlias}${apiPath}/${model.camelName}';
  import { useDrawer } from 'fe-ent-core/es/components/drawer';
  import { NPopconfirm } from 'naive-ui';
  import { useMessage, usePermission } from 'fe-ent-core/es/hooks';
  import ${model.name}DetailDrawer from './detail.vue';
  import ${model.name}EditDrawer from './edit.vue';
  import { columns, searchFormSchema } from './data';
  import type { Recordable } from 'fe-ent-core/es/types';

  export default defineComponent({
    name: '${model.name}Management',
    components: {
      EntTable,
      ${model.name}DetailDrawer,
      ${model.name}EditDrawer,
      NPopconfirm
    },
    setup() {
      const { createMessage } = useMessage();
      const { hasPermission } = usePermission();
      const [registerEditDrawer, { openDrawer: openEditDrawer }] = useDrawer();
      const [registerDetailDrawer, { openDrawer: openDetailDrawer }] = useDrawer();
      const checkedKeys = ref<Array<string | number>>([]);
      const searchInfo = reactive<Recordable>({});
      const [registerTable, { getSelectRows, reload }] = useTable({
        title: '${model.description}列表',
        api: ${model.name}Page,
        rowKey: (record) => record.${pk.name},
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
          autoSubmitOnEnter: true
        },
        useSearchForm: <#if (searchFields?size>0)>true<#else>false</#if>,
        showTableSetting: true,
        bordered: true,
        pagination: {
          page: 1,
          pageSize: 10
        },
        handleSearchInfoFn(info) {
          return info;
        },
        beforeFetch(params) {
          const { pagination, searchForm = {}, sorter } = params;
          const query: Recordable = {};
<#if (searchFields?size>0)>
          <#list searchFields as field>
          <#if (field.fieldType == 'date-time' || field.fieldType == 'date')>
          if (searchForm.${field.name}) {
            query.searchTimeField = '${field.name}';
            query.searchBeginTime = searchForm.${field.name}[0];
            query.searchEndTime = searchForm.${field.name}[1];
            searchForm.${field.name} = undefined;
          }
          </#if>
          </#list>
</#if>
          return {
            ...searchForm,
            _query: { ...pagination, ...query, ...sorter }
          };
        },
        actionColumn: {
          width: 150,
          title: '操作',
          key: 'action',
          render: (record) => {
            return h(
              EntTableAction,
              {
                actions: [
                  {
                    icon: 'ant-design:unordered-list-outlined',
                    tooltip: '查看${model.description}详情',
                    onClick: handleView.bind(null, record),
                    ifShow: () => {
                      return hasPermission('${model.camelName}:detail');
                    }
                  },
                  {
                    icon: 'ant-design:edit-outlined',
                    tooltip: '编辑${model.description}资料',
                    onClick: handleEdit.bind(null, record),
                    ifShow: () => {
                      return hasPermission('${model.camelName}:update');
                    }
                  },
                  {
                    icon: 'ant-design:delete-outlined',
                    tooltip: '删除此${model.description}',
                    confirm: '是否确认删除?',
                    onClick: handleDelete.bind(null, record),
                    ifShow: () => {
                      return hasPermission('${model.camelName}:delete');
                    }
                  }
                ]
              },
              { default: () => '' }
            );
          }
        }
      });

      function handleCreate() {
        openEditDrawer(true, {
          edit_mode: 'c'
        });
      }

      function handleEdit(record: Recordable) {
        openEditDrawer(true, {
          record,
          edit_mode: 'u'
        });
      }

      function handleDelete(record: Recordable) {
        ${model.name}Delete(record).then(() => {
          createMessage.success(`删除成功`);
          reload();
        });
      }

      function handleEditSuccess() {
        //刷新表格直接更新内部数据。
        reload();
      }

      function handleView(record: Recordable) {
        openDetailDrawer(true, {
          record,
          edit_mode: 'r'
        });
      }

      function onSelectChange(selectedRowKeys: (string | number)[]) {
        checkedKeys.value = selectedRowKeys;
      }

      function handleBatchDelete() {
        const records = getSelectRows();
        ${model.name}BatchDelete(records).then(() => {
          createMessage.success(`删除成功`);
          checkedKeys.value = [];
          reload();
        });
      }

      return {
        registerTable,
        registerEditDrawer,
        registerDetailDrawer,
        hasPermission,
        handleCreate,
        handleEditSuccess,
        searchInfo,
        checkedKeys,
        onSelectChange,
        handleBatchDelete
      };
    }
  });
</script>
