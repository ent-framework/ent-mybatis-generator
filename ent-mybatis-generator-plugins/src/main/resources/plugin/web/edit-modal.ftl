<template>
  <EntDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    showFooter
    :title="getTitle"
    width="500px"
    @ok="handleSubmit"
  >
    <EntForm @register="registerForm" />
  </EntDrawer>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref } from 'vue';
  import { EntForm, useForm } from 'fe-ent-core/es/components/form';
  import { formSchema } from './${model.camelModelName}.data';
  import { EntDrawer, useDrawerInner } from 'fe-ent-core/es/components/drawer';
  import { useMessage } from 'fe-ent-core/es/hooks/web/use-message';
  import { ${model.name}Create, ${model.name}Update } from '${projectRootAlias}${apiPath}/${model.camelModelName}';

  export default defineComponent({
    name: '${model.name}EditModal',
    components: { EntDrawer, EntForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const mode = ref('c');
      const { createMessage } = useMessage();
      const ${pk.name} = ref(null);
      const [registerForm, { resetFields, setFieldsValue, validate, setProps }] = useForm({
        labelWidth: 90,
        schemas: formSchema,
        showActionButtonGroup: false,
      });

      const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
        await resetFields();
        setDrawerProps({ confirmLoading: false, destroyOnClose: true });
        mode.value = data?.edit_mode;

        if (unref(mode) === 'u' || unref(mode) === 'r') {
          ${pk.name}.value = data.record.${pk.name};
          await setFieldsValue({
            ...data.record,
          });
          if (unref(mode) === 'r') {
            await setProps({ disabled: true });
          } else {
            await setProps({ disabled: false });
          }
        }
      });

      const getTitle = computed(() => {
        const v = unref(mode);
        switch (v) {
          case 'c':
            return '新增${model.modelDescription}';
          case 'u':
            return '编辑${model.modelDescription}';
          case 'r':
            return '查看${model.modelDescription}';
        }
        return '';
      });

      async function handleSubmit() {
        try {
          const values = await validate();
          setDrawerProps({ confirmLoading: true });
          if (unref(mode) == 'u') {
            ${model.name}Update({ ...values, ${pk.name}: ${pk.name}.value })
              .then(() => {
                createMessage.success(`保存成功`);
                closeDrawer();
                emit('success');
              })
              .catch();
          } else if (unref(mode) == 'c') {
            ${model.name}Create({ ...values, ${pk.name}: null })
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
