<template>
  <div>
    <EntTable
      :search-info="searchInfo"
      :row-selection="{ type: 'checkbox', selectedRowKeys: checkedKeys, onChange: onSelectChange }"
      @register="registerTable"
    >
      <template #headerTop>
        <Alert v-if="checkedKeys.length > 0" type="info" show-icon>
          <template #message>
            <span>已选中{{ checkedKeys.length }}条记录(可跨页)</span>
            <ent-button type="link" @click="checkedKeys = []" size="small">清空</ent-button>
          </template>
        </Alert>
      </template>
      <template #toolbar>
        <Popconfirm
          title="确认删除所选记录?"
          ok-text="Yes"
          cancel-text="No"
          @confirm="handleBatchDelete"
        >
          <ent-button type="primary" danger :disabled="checkedKeys.length === 0">删除</ent-button>
        </Popconfirm>
        <ent-button type="primary" @click="handleCreate">新增${modelDescription}</ent-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <EntTableAction
            :actions="[
              {
                icon: 'clarity:info-standard-line',
                tooltip: '查看${modelDescription}详情',
                onClick: handleView.bind(null, record),
              },
              {
                icon: 'clarity:note-edit-line',
                tooltip: '编辑${modelDescription}资料',
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'ant-design:delete-outlined',
                color: 'error',
                tooltip: '删除此${modelDescription}',
                popConfirm: {
                  title: '是否确认删除',
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
          />
        </template>
      </template>
    </EntTable>
    <${modelName}EditDrawer @register="registerEditDrawer" @success="handleEditSuccess" />
    <${modelName}DetailDrawer @register="registerDetailDrawer" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, reactive, ref } from 'vue';
  import { EntTable, EntTableAction, useTable } from 'fe-ent-core/es/components/table';
  import { ${modelName}BatchDelete, ${modelName}Delete, ${modelName}Page } from '${projectRootAlias}${apiPath}/${camelModelName}';
  import { useDrawer } from 'fe-ent-core/es/components/drawer';
  import { Alert, Popconfirm } from 'ant-design-vue';
  import { useMessage } from 'fe-ent-core/es/hooks/web/use-message';
  import ${modelName}DetailDrawer from './detail.vue';
  import ${modelName}EditDrawer from './edit.vue';
  import { columns, searchFormSchema } from './data';
  import type { Recordable } from 'fe-ent-core/es/types';

  export default defineComponent({
    name: '${modelName}Management',
    components: {
      EntTable,
      ${modelName}DetailDrawer,
      ${modelName}EditDrawer,
      EntTableAction,
      Alert,
      Popconfirm,
    },
    setup() {
      const { createMessage } = useMessage();
      const [registerEditDrawer, { openDrawer: openEditDrawer }] = useDrawer();
      const [registerDetailDrawer, { openDrawer: openDetailDrawer }] = useDrawer();
      const checkedKeys = ref<Array<string | number>>([]);
      const searchInfo = reactive<Recordable>({});
      const [registerTable, { getSelectRows, reload }] = useTable({
        title: '${modelDescription}列表',
        api: ${modelName}Page,
        rowKey: '${pk.name}',
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
          autoSubmitOnEnter: true,
        },
        useSearchForm: <#if (searchFields?size>0)>true<#else>false</#if>,
        showTableSetting: true,
        bordered: true,
        handleSearchInfoFn(info) {
          return info;
        },
<#if (searchFields?size>0)>
        beforeFetch(params) {
          <#list searchFields as field>
          <#if (field.fieldType == 'date-time' || field.fieldType == 'date')>
          if (params.${field.name}) {
            params.searchTimeField = '${field.name}';
            params.searchBeginTime = params.${field.name}[0];
            params.searchEndTime = params.${field.name}[1];
            params.${field.name} = undefined;
          }
          </#if>
          </#list>
          return params;
        },
</#if>
        actionColumn: {
          width: 120,
          title: '操作',
          dataIndex: 'action',
        },
      });

      function handleCreate() {
        openEditDrawer(true, {
          edit_mode: 'c',
        });
      }

      function handleEdit(record: Recordable) {
        openEditDrawer(true, {
          record,
          edit_mode: 'u',
        });
      }

      function handleDelete(record: Recordable) {
        ${modelName}Delete(record).then(() => {
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
          edit_mode: 'r',
        });
      }

      function onSelectChange(selectedRowKeys: (string | number)[]) {
        checkedKeys.value = selectedRowKeys;
      }

      function handleBatchDelete() {
        const records = getSelectRows();
        ${modelName}BatchDelete(records).then(() => {
          createMessage.success(`删除成功`);
          checkedKeys.value = [];
          reload();
        });
      }

      return {
        registerTable,
        registerEditDrawer,
        registerDetailDrawer,
        handleCreate,
        handleEdit,
        handleDelete,
        handleEditSuccess,
        handleView,
        searchInfo,
        checkedKeys,
        onSelectChange,
        handleBatchDelete,
      };
    },
  });
</script>
