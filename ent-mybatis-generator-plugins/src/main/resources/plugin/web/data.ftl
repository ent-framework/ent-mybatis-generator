<#if model.tenant>
import { usePermission } from 'fe-ent-core/es/hooks';
</#if>
<#if (model.enumLabel || model.enumSwitch)>
import { h } from 'vue';
</#if>
<#if (model.enumLabel && model.enumSwitch)>
import { Switch, Tag } from 'ant-design-vue';
<#elseif model.enumLabel>
import { Tag } from 'ant-design-vue';
<#elseif model.enumSwitch>
import { Switch } from 'ant-design-vue';
</#if>
<#if model.enumSwitch>
import { useMessage } from 'fe-ent-core/es/hooks';
import { ${model.name}Update } from '${projectRootAlias}${apiPath}/${model.camelName}';
</#if>
import type { BasicColumn } from 'fe-ent-core/es/components/table/interface';
import type { FormSchema } from 'fe-ent-core/es/components/form/interface';
import type { DescItem } from 'fe-ent-core/es/components/description/interface';
<#if model.tenant>
import { TenantList } from '${projectRootAlias}/api/tenant';
</#if>
<#list enumFields as field>
import { ${field.javaType.shortName}Types } from '${field.javaType.packagePath}';
</#list>
<#list relationFields as field>
<#if field.manyToOne>
import { ${field.javaType.shortName}List } from '${projectRootAlias}${apiPath}/${field.javaType.fileName}';
</#if>
</#list>

export const columns: BasicColumn[] = [
<#list listFields as field>
  {
    title: '${field.description}',
<#if field.fieldType == 'relation'>
    dataIndex: '${field.name}.${field.relation.displayField}',
<#else>
    dataIndex: '${field.name}',
</#if>
<#if field.hidden>
    defaultHidden: true,
</#if>
<#if (field.relationField && field.manyToOne)>
    width: 110,
<#elseif field.fieldType == 'enum'>
    width: 120,
  <#if (field.enumLabel && field.enumLabelType == 'Status')>
    customRender: ({ record }) => {
      const status = record.${field.name};
      const enable = Math.trunc(status) === 1;
      const color = enable ? 'green' : 'red';
      const text = enable ? '启用' : '停用';
      return h(Tag, { color }, () => text);
    },
  <#elseif (field.enumLabel && field.enumLabelType == 'YesOrNot')>
    customRender: ({ record }) => {
      const status = record.${field.name};
      const enable = status === 'Y';
      const color = enable ? 'green' : 'red';
      const text = enable ? '是' : '否';
      return h(Tag, { color }, () => text);
    },
  <#elseif (field.enumSwitch && field.enumSwitchType == 'Status')>
    customRender: ({ record }) => {
      if (!Reflect.has(record, 'pendingStatus')) {
        record.pendingStatus = false;
      }
      return h(Switch, {
        checked: record.${field.name} === 1,
        checkedChildren: '启用',
        unCheckedChildren: '禁用',
        loading: record.pendingStatus,
        onChange(checked: boolean) {
          record.pendingStatus = true;
          const newStatus = checked ? 1 : 2;
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
        },
      });
    },
  <#elseif (field.enumSwitch && field.enumSwitchType == 'YesOrNot')>
    customRender: ({ record }) => {
      if (!Reflect.has(record, 'pendingStatus')) {
        record.pendingStatus = false;
      }
      return h(Switch, {
        checked: record.${field.name} === 'Y',
        checkedChildren: '是',
        unCheckedChildren: '否',
        loading: record.pendingStatus,
        onChange(checked: boolean) {
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
        },
      });
    },
  <#else>
    customRender: ({ value }) => {
      const enumType = ${field.javaType.shortName}Types.find((v) => v.value === value);
      return enumType ? enumType.label : value;
    },
  </#if>
<#else>
</#if>
  },
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
      options: ${field.javaType.shortName}Types,
    },
    colProps: { span: 6 },
<#elseif field.fieldType == 'date'>
    component: 'RangePicker',
    colProps: { span: 8 },
<#elseif field.fieldType == 'date-time'>
    component: 'RangePicker',
    componentProps: {
      'show-time': true,
    },
    colProps: { span: 8 },
<#elseif field.fieldType == 'relation'>
    colProps: { span: 6 },
    component: 'ApiSelect',
    componentProps: {
      api: ${field.relation.bindField.type.shortName}List,
      resultField: 'items',
      // use name as label
      labelField: '${field.relation.displayField}',
      // use id as value
      valueField: '${field.relation.targetColumn.javaProperty}',
      // not request until to select
      immediate: true,
    },
<#elseif field.tenantField>
    colProps: { span: 6 },
    ifShow: () => {
      const { hasPermission } = usePermission();
      return hasPermission('ROLE_ADMINISTRATOR') || hasPermission('tenant:list');
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
      immediate: true,
    },
<#else >
    component: 'Input',
    colProps: { span: 6 },
</#if>
  },
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
    labelMinWidth: 100,
    labelStyle: {
      'text-align': 'end',
    },
    render: (val) => {
      const enumType = ${field.javaType.shortName}Types.find((v) => v.value === val);
      return enumType ? enumType.label : val;
    },
  },
<#else>
  {
    label: '${field.description}',
    field: '${field.name}',
    labelMinWidth: 100,
    labelStyle: {
      'text-align': 'end',
    },
  },
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
    colProps: {
      span: 24,
    },
<#if field.fieldType == 'enum'>
    componentProps: {
      options: ${field.javaType.shortName}Types,
    },
<#elseif field.fieldType == 'clob'>
    componentProps: {
      rows: 10,
    },
<#elseif field.fieldType == 'relation'>
    componentProps: {
      api: ${field.relation.bindField.type.shortName}List,
      resultField: 'items',
      // use name as label
      labelField: '${field.relation.displayField}',
      // use id as value
      valueField: '${field.relation.targetColumn.javaProperty}',
      // not request until to select
      immediate: true,
    },
</#if>    
  },
</#list>
];
