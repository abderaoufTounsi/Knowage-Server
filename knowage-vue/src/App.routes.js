import { createRouter, createWebHistory } from 'vue-router'
import IframeRenderer from '@/modules/commons/IframeRenderer.vue'
import managersRoutes from '@/modules/managers/managers.routes.js'
import importExportRoutes from '@/modules/importExport/ImportExport.routes.js'
import kpiRoutes from '@/modules/kpi/kpi.routes.js'
import documentExecutionRoutes from '@/modules/documentExecution/documentExecution.routes.js'
import documentBrowserRoutes from '@/modules/documentBrowser/DocumentBrowser.routes.js'
import workspaceRoutes from '@/modules/workspace/workspace.routes.js'
import overlayRoutes from '@/overlay/Overlay.routes.js'
import authHelper from '@/helpers/commons/authHelper'
import dataPreparationRoutes from '@/modules/workspace/dataPreparation/DataPreparation.routes.js'
import { loadLanguageAsync } from '@/App.i18n.js'
import { getCorrectRolesForExecutionForType } from '@/helpers/commons/roleHelper'

const baseRoutes = [
    {
        path: '/',
        name: 'home',
        component: () => import('@/views/Home.vue')
    },
    {
        path: '/about',
        name: 'about',
        component: () => import('@/views/About.vue')
    },
    {
        path: '/externalUrl/',
        name: 'externalUrl',
        component: IframeRenderer,
        props: (route) => ({ url: route.params.url, externalLink: true })
    },
    {
        path: '/knowage/servlet/:catchAll(.*)',
        name: 'knowageUrl',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/knowage/restful-services/publish:catchAll(.*)',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/knowage/restful-services/signup:catchAll(.*)',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/knowage/restful-services/2.0/installconfig',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/knowage/themes:catchAll(.*)',
        component: IframeRenderer,
        props: (route) => ({ url: route.fullPath })
    },
    {
        path: '/login',
        name: 'login',
        redirect: import.meta.env.VITE_HOST_URL + '/knowage/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE'
    },
    {
        path: '/:catchAll(.*)',
        component: () => import('@/modules/commons/404.vue')
    }
]

const routes = baseRoutes.concat(managersRoutes).concat(importExportRoutes).concat(kpiRoutes).concat(documentExecutionRoutes).concat(documentBrowserRoutes).concat(workspaceRoutes).concat(overlayRoutes).concat(dataPreparationRoutes)

const router = createRouter({
    base: import.meta.env.VITE_PUBLIC_PATH,
    history: createWebHistory(import.meta.env.VITE_PUBLIC_PATH),
    routes
})

router.afterEach(async (to, from) => {
    if (localStorage.getItem('locale')) loadLanguageAsync(localStorage.getItem('locale'))
})

router.beforeEach((to, from, next) => {
    if (localStorage.getItem('locale')) loadLanguageAsync(localStorage.getItem('locale')).then(() => next())
    const checkRequired = !('/' == to.fullPath && '/' == from.fullPath)
    const loggedIn = localStorage.getItem('token')

    const validRoutes = ['registry', 'document-composite', 'report', 'office-doc', 'olap', 'map', 'report', '/kpi/', 'dossier', 'etl']
    const invalidRoutes = ['olap-designer']
    if (checkRequired && !loggedIn) {
        authHelper.handleUnauthorized()
    } else if (validRoutes.some((el) => to.fullPath.includes(el)) && !invalidRoutes.some((el) => to.fullPath.includes(el))) {
        getCorrectRolesForExecutionForType('DOCUMENT', null, to.params.id).then(() => {
            next()
        })
    } else {
        next()
    }
})

export default router
