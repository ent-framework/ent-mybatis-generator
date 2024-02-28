<template>
  <EntDrawer
    v-bind="$attrs"
    show-footer
    title="查看${modelDescription}"
    width="640px"
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
  import { ${modelName}Load } from '${projectRootAlias}${apiPath}/${camelModelName}';
  import { detailSchema } from './data';
  import type { ${modelName} } from '${projectRootAlias}${modelPath}/${camelModelName}';

  export default defineComponent({
    name: '${modelName}DetailDrawer',
    components: { EntDrawer, EntDescription },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const detailData = ref<${modelName}>();

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        //detailData.value = data.record;
        try {
          setDrawerProps({ confirmLoading: true });
          detailData.value = await ${modelName}Load({ ${pk.name}: data.record.${pk.name} });
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
