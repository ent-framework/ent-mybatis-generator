<template>
  <EntDrawer
    v-bind="$attrs"
    width="500px"
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
<#if (clobFields?size>0)>
  import { unescape } from 'lodash';
</#if>
  import { formSchema } from './data';
  import { EntDrawer, useDrawerInner } from 'fe-ent-core/es/components/drawer';
  import { useMessage } from 'fe-ent-core/es/hooks/web/use-message';
  import { ${model.name}Insert, ${model.name}Load, ${model.name}Update } from '${projectRootAlias}${apiPath}/${model.camelName}';

  export default defineComponent({
    name: '${model.name}EditDrawer',
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
<#if (clobFields?size>0)>
            const detail = await ${model.name}Load(
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
            const detail = await ${model.name}Load({ ${pk.name}: data.record.${pk.name} });
</#if>            
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
            return '新增${model.description}';
          case 'u':
            return '编辑${model.description}';
          case 'r':
            return '查看${model.description}';
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
            ${model.name}Insert({ ...values, ${pk.name}: null })
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
