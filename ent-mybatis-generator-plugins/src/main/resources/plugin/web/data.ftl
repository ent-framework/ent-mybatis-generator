<#if (model.tenant || model.enumSwitch)>
import { usePermission } from 'fe-ent-core/es/hooks/web/use-permission';
</#if>
<#if (model.enumLabel || model.enumSwitch)>
import { h } from 'vue';
import { NSwitch, NTag } from 'naive-ui';
</#if>
<#if model.enumSwitch>
import { useMessage } from 'fe-ent-core/es/hooks/web/use-message';
import { ${model.name}Update } from '${projectRootAlias}${apiPath}/${model.camelName}';
</#if>
import type { BasicColumn } from 'fe-ent-core/es/components/table/interface';
import type { FormSchema } from 'fe-ent-core/es/components/form/interface';
import type { DescItem } from 'fe-ent-core/es/components/description/interface';
<#if model.tenant>
import { TenantList } from '${projectRootAlias}api/tenant';
</#if>
<#list enumFieldImport as fi>
import { ${fi.shortName}Types } from '${fi.packagePath}';
</#list>
<#list relationFields as field>
<#if field.manyToOne>
import { ${field.javaType.shortName}List } from '${projectRootAlias}${apiPath}/${field.javaType.fileName}';
</#if>
</#list>

export const columns: BasicColumn[] = [
<#list listFields as field>
  {
<#if field.fieldType == 'relation'>
    key: '${field.name}.${field.relation.displayField}',
  <#if (field.relationField && field.manyToOne)>
    width: 110,
  </#if>
<#elseif field.fieldType == 'enum'>
    key: '${field.name}',
    width: 120,
    className: 'ent-table-edit-cell',
  <#if (field.enumLabel && field.enumLabelType == 'Status')>
    render: (record) => {
      const status = record.${field.name};
      const enable = Math.trunc(status) === 1;
      const type = enable ? 'success' : 'warning';
      const text = enable ? '启用' : '停用';
      return h(NTag, { type }, () => text);
    },
  <#elseif (field.enumLabel && field.enumLabelType == 'YesOrNot')>
    render: (record) => {
      const status = record.${field.name};
      const enable = status === 'Y';
      const type = enable ? 'success' : 'warning';
      const text = enable ? '是' : '否';
      return h(NTag, { type }, () => text);
    },
  <#elseif (field.enumSwitch && field.enumSwitchType == 'Status')>
    render: (record) => {
      const { hasPermission } = usePermission();
      const canEdit = hasPermission('${model.camelName}:update');
      if (!canEdit) {
        const status = record.${field.name};
        const enable = Math.trunc(status) === 1;
        const type = enable ? 'success' : 'warning';
        const text = enable ? '启用' : '停用';
        return h(NTag, { type }, () => text);
      }
      if (!Reflect.has(record, 'pendingStatus')) {
        record.pendingStatus = false;
      }
      return h(NSwitch, {
        checkedValue: 1,
        unCheckedValue: 2,
        value: record.${field.name},
        loading: record.pendingStatus,
        onUpdateValue(checked: boolean) {
          record.pendingStatus = true;
          const newStatus = checked ? 1 : 2;
          const { createMessage } = useMessage();
          ${model.name}Update({ ...record, ${field.name}: newStatus })
            .then((result) => {
          <#if model.versionField ??>
              if (result.${model.versionField}) {
                record.${model.versionField} = result.${model.versionField};
              }
          </#if>
              record.${field.name} = newStatus;
              createMessage.success(`已成功修改${field.description}`);
            })
            .catch(() => {
              createMessage.error('修改${field.description}失败');
            })
            .finally(() => {
              record.pendingStatus = false;
            });
        }
      });
    },
  <#elseif (field.enumSwitch && field.enumSwitchType == 'YesOrNot')>
    render: (record) => {
      const { hasPermission } = usePermission();
      const canEdit = hasPermission('${model.camelName}:update');
      if (!canEdit) {
        const status = record.${field.name};
        const enable = status === 'Y';
        const type = enable ? 'success' : 'warning';
        const text = enable ? '是' : '否';
        return h(NTag, { type }, () => text);
      }
      if (!Reflect.has(record, 'pendingStatus')) {
        record.pendingStatus = false;
      }
      return h(NSwitch, {
        checked: record.${field.name} === 'Y',
        checkedValue: 'Y',
        unCheckedValue: 'N',
        value: record.${field.name},
        loading: record.pendingStatus,
        onUpdateValue(checked: boolean) {
          record.pendingStatus = true;
          const newStatus = checked ? 'Y' : 'N';
          const { createMessage } = useMessage();
          ${model.name}Update({ ...record, ${field.name}: newStatus })
            .then(() => {
              record.${field.name} = newStatus;
              createMessage.success(`已成功修改${field.description}`);
            })
            .catch(() => {
              createMessage.error('修改${field.description}失败');
            })
            .finally(() => {
              record.pendingStatus = false;
            });
        }
      });
    },
  <#else>
    render: (record) => {
      const enumType = ${field.javaType.shortName}Types.find((v) => v.value === record.${field.name});
      return enumType ? enumType.label : record.${field.name};
    },
  </#if>
<#else>
    key: '${field.name}',
</#if>
<#if (field.fieldType == 'number' || field.fieldType == 'date-time')>
    sorter: true,
</#if>
<#if field.hidden>
    defaultHidden: true,
</#if>
    title: '${field.description}'
  }<#if (field?has_next)>,</#if>
</#list>
];
<#if (searchFields?size>0)>
export const searchFormSchema: FormSchema[] = [
<#list searchFields as field>
  {
<#if field.fieldType == 'relation'>
    field: '${field.relation.sourceField.name}',
<#else>
    field: '${field.name}',
</#if>
    label: '${field.description}',
<#if field.fieldType == 'enum'>
    component: '${field.inputType}',
    componentProps: {
      options: ${field.javaType.shortName}Types
    },
    gridItemProps: { span: 6 }
<#elseif field.fieldType == 'date'>
    component: 'RangePicker',
    gridItemProps: { span: 8 }
<#elseif field.fieldType == 'date-time'>
    component: 'DateTimeRangePicker',
    gridItemProps: { span: 8 }
<#elseif field.fieldType == 'relation'>
    gridItemProps: { span: 6 },
    component: 'ApiSelect',
    componentProps: {
      api: ${field.relation.bindField.type.shortName}List,
      resultField: 'items',
      // use name as label
      labelField: '${field.relation.displayField}',
      // use id as value
      valueField: '${field.relation.targetColumn.javaProperty}',
      // not request until to select
      immediate: true
    }
<#elseif field.tenantField>
    gridItemProps: { span: 6 },
    ifShow: () => {
      const { hasPermission } = usePermission();
      return hasPermission('ROLE_ADMINISTRATOR') || hasPermission('tenant:query');
    },
    component: 'ApiSelect',
    componentProps: {
      api: TenantList,
      resultField: 'items',
      // use name as label
      labelField: 'name',
      // use id as value
      valueField: 'id',
      // not request until to select
      immediate: true
    }
<#else >
    component: 'Input',
    gridItemProps: { span: 6 }
</#if>
  }<#if (field?has_next)>,</#if>
</#list>
];
<#else>
export const searchFormSchema: FormSchema[] = [];
</#if>

export const detailSchema: DescItem[] = [
<#list detailFields as field>
<#if field.fieldType == 'enum'>
  {
    label: '${field.description}',
    field: '${field.name}',
    render: (val) => {
      const enumType = ${field.javaType.shortName}Types.find((v) => v.value === val);
      return enumType ? enumType.label : val;
    }
  }<#if (field?has_next)>,</#if>
<#else>
  {
    label: '${field.description}',
    field: '${field.name}'
  }<#if (field?has_next)>,</#if>
</#if>
</#list>
];

export const formSchema: FormSchema[] = [
<#list inputFields as field>
  {
<#if field.fieldType == 'relation'>
    field: '${field.relation.sourceField.name}',
<#else>
    field: '${field.name}',
</#if>
    label: '${field.description}',
    labelWidth: '100px',
    component: '${field.inputType}',
<#if field.hidden>
    show: false,
</#if>
<#if field.required>
    required: ${field.required?c},
</#if>
<#-- 针对字段类型特殊处理 -->
<#if field.fieldType == 'enum'>
    gridItemProps: {
      span: 24
    },
    componentProps: {
      options: ${field.javaType.shortName}Types
    }
<#elseif field.fieldType == 'clob'>
    gridItemProps: {
      span: 24
    },
    componentProps: {
      rows: 10
    }
<#elseif field.fieldType == 'date'>
    gridItemProps: {
      span: 24
    },
    componentProps: {
      rows: 10
    }
<#elseif field.fieldType == 'date-time'>
    gridItemProps: {
      span: 24
    },
    componentProps: {
      rows: 10
    }
<#elseif field.fieldType == 'relation'>
    gridItemProps: {
      span: 24
    },
    componentProps: {
      api: ${field.relation.bindField.type.shortName}List,
      resultField: 'items',
      // use name as label
      labelField: '${field.relation.displayField}',
      // use id as value
      valueField: '${field.relation.targetColumn.javaProperty}',
      // not request until to select
      immediate: true
    }
<#elseif field.tenantField>
    gridItemProps: {
      span: 24
    },
    ifShow: () => {
      const { hasPermission } = usePermission();
      return hasPermission('ROLE_ADMINISTRATOR') || hasPermission('tenant:assign');
    },
    componentProps: {
      api: TenantList,
      resultField: 'items',
      // use name as label
      labelField: 'name',
      // use id as value
      valueField: 'id',
      // not request until to select
      immediate: true
    }
<#else>
    gridItemProps: {
      span: 24
    }
</#if>
  }<#if (field?has_next)>,</#if>
</#list>
];
