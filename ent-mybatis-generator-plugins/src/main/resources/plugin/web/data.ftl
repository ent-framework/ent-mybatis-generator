import type { BasicColumn } from 'fe-ent-core/es/components/table/interface';
import type { FormSchema } from 'fe-ent-core/es/components/form/interface';
import type { DescItem } from 'fe-ent-core/es/components/description/interface';
<#list enumFields as field>
import { ${field.javaType.shortName}Types } from '${field.javaType.packagePath}';
</#list>
<#list relationFields as field>
import { ${field.javaType.shortName}List } from '${projectRootAlias}${apiPath}/${field.javaType.fileName}';
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
    customRender: ({ value }) => {
      const enumType = ${field.javaType.shortName}Types.find((v) => v.value === value);
      return enumType ? enumType.label : value;
    },
<#else>
</#if>
  },
</#list>
];
<#if (searchFields?size>0)>
export const searchFormSchema: FormSchema[] = [
<#list searchFields as field>
  {
    field: '${field.name}',
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
    render: (val) => {
      const enumType = ${field.javaType.shortName}Types.find((v) => v.value === val);
      return enumType ? enumType.label : val;
    },
  },
<#else>
  {
    label: '${field.description}',
    field: '${field.name}',
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
    labelWidth: '120px',
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
