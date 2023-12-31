import type { BasicColumn } from 'fe-ent-core/es/components/table/interface';
import type { FormSchema } from 'fe-ent-core/es/components/form/interface';
import type { DescItem } from 'fe-ent-core/es/components/description/interface';
<#list enumFields as field>
  import { ${field.javaType.shortName}Options } from '${field.javaType.packagePath}';
</#list>
<#list relationFields as field>
import { ${field.javaType.shortName}List } from '${projectRootAlias}${apiPath}/${field.javaType.fileName}';
</#list>

export const columns: BasicColumn[] = [
<#list listFields as field>
<#if field.relationField && field.manyToOne>
  {
    title: '${field.description}',
    dataIndex: '${field.name}',
    width: 110,
  },
<#else>
  {
    title: '${field.description}',
    dataIndex: '${field.name}',
    width: 120,
  },
</#if>
</#list>
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'account',
    label: '用户名',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'nickname',
    label: '昵称',
    component: 'Input',
    colProps: { span: 8 },
  },
];

export const detailSchema: DescItem[] = [
<#list fields as field>
  {
    label: '${field.description}',
    field: '${field.name}',
  },
</#list>
];

export const formSchema: FormSchema[] = [
<#list inputFields as field>
  <#assign inputType="Input"/>
  <#if field.fieldType == 'number'>
    <#assign inputType="InputNumber"/>
  <#elseif field.fieldType == 'date'>
    <#assign inputType="DatePicker"/>
  <#elseif field.fieldType == 'time'>
    <#assign inputType="TimePicker"/>
  <#elseif field.fieldType == 'date-time'>
    <#assign inputType="DatePicker"/>        
  <#elseif field.fieldType == 'boolean'>
    <#assign inputType="Switch"/>
  <#elseif field.fieldType == 'enum'>
    <#assign inputType="Select"/>         
  <#elseif field.fieldType == 'relation'>
    <#assign inputType="ApiSelect"/>       
  </#if>
  {
    field: '${field.name}',
    label: '${field.description}',
    component: '${inputType}',
    required: ${field.required?c},
<#if field.fieldType == 'enum'>
    componentProps: {
      options: ${field.javaType.shortName}Options,
    },
<#elseif field.fieldType == 'relation'>
    componentProps: {
      // more details see /src/components/Form/src/components/ApiSelect.vue
      api: ${field.targetRelation.bindField.type.shortName}List,
      resultField: 'items',
      // use name as label
      labelField: '${field.targetRelation.displayField}',
      // use id as value
      valueField: '${field.targetRelation.targetColumn.javaProperty}',
      // not request untill to select
      immediate: true,
    },
</#if>    
  },
</#list>
];
