import type { AppRouteRecordRaw } from 'fe-ent-core/es/router/types';

const routes: AppRouteRecordRaw = {
  path: '${routerPrefixPath}',
  name: '${basicRouterName!'BaseRouteData'}',
  component: 'LAYOUT',
  meta: {
    icon: 'simple-icons:about-dot-me',
    title: '${basicRouterTitle!''}',
    orderNo: 20,
  },
  children: [
 <#list models as model>
    {
      path: '${model.camelModelName}',
      name: '${model.name}Management',
      component: () => import('${projectRootAlias}${viewPath}/${model.camelModelName}/index.vue'),
      meta: {
        title: '${model.description}',
        icon: 'simple-icons:about-dot-me',
      },
    },
</#list>
  ],
};

export default routes;
