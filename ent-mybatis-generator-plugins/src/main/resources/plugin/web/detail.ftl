<template>
  <EntDrawer
    v-bind="$attrs"
    show-footer
    title="查看${modelDescription}"
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
  import { detailSchema } from './data';

  export default defineComponent({
    name: 'LogDrawer',
    components: { EntDrawer, EntDescription },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const detailData = ref(null);

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        setDrawerProps({ confirmLoading: false });
        detailData.value = data.record;
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
