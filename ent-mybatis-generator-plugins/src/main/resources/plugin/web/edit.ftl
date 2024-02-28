<template>
  <EntDrawer
    v-bind="$attrs"
    width="640px"
    show-footer
    :title="getTitle"
    @register="registerDrawer"
    @ok="handleSubmit"
  >
    <EntForm @register="registerForm" />
  </EntDrawer>
</template>
<script lang="ts">
  import { computed, defineComponent, ref, unref } from 'vue';
  import { EntForm, useForm } from 'fe-ent-core/es/components/form';
  import { formSchema } from './data';
  import { EntDrawer, useDrawerInner } from 'fe-ent-core/es/components/drawer';
  import { useMessage } from 'fe-ent-core/es/hooks/web/use-message';
  import { ${modelName}Insert, ${modelName}Load, ${modelName}Update } from '${projectRootAlias}${apiPath}/${camelModelName}';

  export default defineComponent({
    name: '${modelName}EditDrawer',
    components: { EntDrawer, EntForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const mode = ref('c');
      const { createMessage } = useMessage();
      const ${pk.name} = ref<any>(null);
      const [registerForm, { resetFields, setFieldsValue, validate, setProps }] = useForm({
        labelWidth: 90,
        schemas: formSchema,
        showActionButtonGroup: false,
      });

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        await resetFields();
        setDrawerProps({ confirmLoading: false, destroyOnClose: true });
        mode.value = data?.edit_mode;
        try {
          if (unref(mode) === 'u' || unref(mode) === 'r') {
            setDrawerProps({ confirmLoading: true });
            const detail = await ${modelName}Load({ ${pk.name}: data.record.${pk.name} });
            ${pk.name}.value = detail.${pk.name};
            await setFieldsValue({
              ...detail,
            });
            if (unref(mode) === 'r') {
              await setProps({ disabled: true });
            } else {
              await setProps({ disabled: false });
            }
          }
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      });

      const getTitle = computed(() => {
        const v = unref(mode);
        switch (v) {
          case 'c':
            return '新增${modelDescription}';
          case 'u':
            return '编辑${modelDescription}';
          case 'r':
            return '查看${modelDescription}';
        }
        return '';
      });

      async function handleSubmit() {
        try {
          const values = await validate();
          setDrawerProps({ confirmLoading: true });
          if (unref(mode) == 'u') {
            ${modelName}Update({ ...values, ${pk.name}: ${pk.name}.value })
              .then(() => {
                createMessage.success(`保存成功`);
                closeDrawer();
                emit('success');
              })
              .catch();
          } else if (unref(mode) == 'c') {
            ${modelName}Insert({ ...values, ${pk.name}: null })
              .then(() => {
                createMessage.success(`保存成功`);
                closeDrawer();
                emit('success');
              })
              .catch();
          }
        } finally {
          setDrawerProps({ confirmLoading: false });
        }
      }

      return {
        registerDrawer,
        registerForm,
        getTitle,
        handleSubmit,
      };
    },
  });
</script>
