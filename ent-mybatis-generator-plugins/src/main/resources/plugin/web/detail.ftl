<template>
  <EntDrawer
    v-bind="$attrs"
    show-footer
    title="查看${model.description}"
    width="500px"
    @register="registerDrawer"
    @ok="handleSubmit"
  >
    <EntDescription
      size="middle"
      :bordered="false"
      :column="1"
      :data="detailData"
      :schema="detailSchema"
    />
  </EntDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { EntDescription } from 'fe-ent-core/es/components/description';
  import { EntDrawer, useDrawerInner } from 'fe-ent-core/es/components/drawer';
  import { ${model.name}Load } from '${projectRootAlias}${apiPath}/${model.camelName}';
<#if (clobFields?size>0)>
  import { unescape } from 'lodash';
</#if>
  import { detailSchema } from './data';
  import type { ${model.name} } from '${projectRootAlias}${model.path}/${model.camelName}';

  export default defineComponent({
    name: '${model.name}DetailDrawer',
    components: { EntDrawer, EntDescription },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const detailData = ref<${model.name}>();

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        //detailData.value = data.record;
        try {
          setDrawerProps({ confirmLoading: true });
<#if (clobFields?size>0)>
          detailData.value = await ${model.name}Load(
            { ${pk.name}: data.record.${pk.name} },
            {
              transformResponse: (data) => {
<#list clobFields as field>
                data.${field.name} = unescape(data.${field.name});
</#list>
                return data;
              },
            },
          );
<#else >
          detailData.value = await ${model.name}Load({ ${pk.name}: data.record.${pk.name} });
</#if>
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      });

      async function handleSubmit() {
        try {
          closeDrawer();
          emit('success');
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }

      return {
        registerDrawer,
        handleSubmit,
        detailSchema,
        detailData,
      };
    },
  });
</script>
