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
<#if (searchFields?size>0)>
export const searchFormSchema: FormSchema[] = [
<#list searchFields as field>
  {
    field: '${field.name}',
    label: '${field.description}',
    component: 'Input',
    colProps: { span: 8 },
  },
</#list>
];
<#else>
export const searchFormSchema: FormSchema[] = [];
</#if>

export const detailSchema: DescItem[] = [
<#list detailFields as field>
  {
    label: '${field.description}',
    field: '${field.name}',
  },
</#list>
];

export const formSchema: FormSchema[] = [
<#list inputFields as field>
  {
    field: '${field.name}',
    label: '${field.description}',
    component: '${field.inputType}',
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
