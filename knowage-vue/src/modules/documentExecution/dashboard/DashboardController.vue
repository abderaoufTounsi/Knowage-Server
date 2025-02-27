<template>
    <div v-if="model" class="dashboard-container" :id="`dashboard_${model.configuration.id}`">
        <Button icon="fas fa-square-check" class="p-m-3 p-button-rounded p-button-text p-button-plain" style="position: fixed; right: 0; z-index: 999; background-color: white; box-shadow: 0px 2px 3px #ccc" @click="selectionsDialogVisible = true" />
        <DashboardRenderer v-if="!loading" :model="model" :datasets="datasets" :dashboardId="dashboardId" :documentDrivers="drivers" :variables="model ? model.configuration.variables : []"></DashboardRenderer>

        <Transition name="editorEnter" appear>
            <DatasetEditor v-if="datasetEditorVisible" :dashboardIdProp="dashboardId" :availableDatasetsProp="datasets" :filtersDataProp="filtersData" @closeDatasetEditor="closeDatasetEditor" @datasetEditorSaved="closeDatasetEditor" @allDatasetsLoaded="datasets = $event" />
        </Transition>

        <Transition name="editorEnter" appear>
            <DashboardGeneralSettings
                v-if="generalSettingsVisible"
                :dashboardId="dashboardId"
                :datasets="datasets"
                :documentDrivers="drivers"
                :profileAttributes="profileAttributes"
                @closeGeneralSettings="closeGeneralSettings"
                @saveGeneralSettings="generalSettingsVisible = false"
            ></DashboardGeneralSettings>
        </Transition>

        <WidgetPickerDialog v-if="widgetPickerVisible" :visible="widgetPickerVisible" @openNewWidgetEditor="openNewWidgetEditor" @closeWidgetPicker="widgetPickerVisible = false" />
        <DashboardControllerSaveDialog v-if="saveDialogVisible" :visible="saveDialogVisible" @save="saveNewDashboard" @close="saveDialogVisible = false"></DashboardControllerSaveDialog>
        <SelectionsListDialog v-if="selectionsDialogVisible" :visible="selectionsDialogVisible" :dashboardId="dashboardId" @close="selectionsDialogVisible = false" @save="onSelectionsRemove" />
    </div>
    <WidgetEditor
        v-if="widgetEditorVisible"
        :dashboardId="dashboardId"
        :propWidget="selectedWidget"
        :datasets="datasets"
        :documentDrivers="drivers"
        :variables="model ? model.configuration.variables : []"
        :htmlGalleryProp="htmlGallery"
        :customChartGalleryProp="customChartGallery"
        @close="closeWidgetEditor"
        @widgetSaved="closeWidgetEditor"
        @widgetUpdated="closeWidgetEditor"
        data-test="widget-editor"
    ></WidgetEditor>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of creating the dashboard instance and to get initializing informations needed like the theme or the datasets.
 */
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { v4 as uuidv4 } from 'uuid'
import { iParameter } from '@/components/UI/KnParameterSidebar/KnParameterSidebar'
import { IDashboardDataset, ISelection, IWidget, IGalleryItem, IDataset } from './Dashboard'
import { emitter, createNewDashboardModel, formatDashboardForSave, formatNewModel, loadDatasets } from './DashboardHelpers'
import { mapActions, mapState } from 'pinia'
import { formatModel } from './helpers/DashboardBackwardCompatibilityHelper'
import { setDatasetIntervals, clearAllDatasetIntervals } from './helpers/datasetRefresh/DatasetRefreshHelpers'
import { loadDrivers } from './helpers/DashboardDriversHelper'
import DashboardRenderer from './DashboardRenderer.vue'
import WidgetPickerDialog from './widget/WidgetPicker/WidgetPickerDialog.vue'
import dashboardStore from './Dashboard.store'
import mainStore from '../../../App.store'
import DatasetEditor from './dataset/DatasetEditor.vue'
import WidgetEditor from './widget/WidgetEditor/WidgetEditor.vue'
import descriptor from './DashboardDescriptor.json'
import cryptoRandomString from 'crypto-random-string'
import DashboardControllerSaveDialog from './DashboardControllerSaveDialog.vue'
import SelectionsListDialog from './widget/SelectorWidget/SelectionsListDialog.vue'
import DashboardGeneralSettings from './generalSettings/DashboardGeneralSettings.vue'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'dashboard-manager',
    components: { DashboardRenderer, WidgetPickerDialog, DatasetEditor, WidgetEditor, DashboardControllerSaveDialog, SelectionsListDialog, DashboardGeneralSettings },
    props: { sbiExecutionId: { type: String }, document: { type: Object }, reloadTrigger: { type: Boolean }, hiddenFormData: { type: Object }, filtersData: { type: Object as PropType<{ filterStatus: iParameter[]; isReadyForExecution: boolean }> }, newDashboardMode: { type: Boolean } },
    emits: ['newDashboardSaved'],
    data() {
        return {
            descriptor,
            model: null as any,
            widgetPickerVisible: false,
            datasetEditorVisible: false,
            datasets: [] as IDataset[],
            widgetEditorVisible: false,
            selectedWidget: null as any,
            crossNavigations: [] as any[],
            profileAttributes: [] as { name: string; value: string }[],
            drivers: [] as any[],
            internationalization: {} as any,
            dashboardId: '',
            saveDialogVisible: false,
            selectionsDialogVisible: false,
            generalSettingsVisible: false,
            loading: false,
            htmlGallery: [] as IGalleryItem[],
            customChartGallery: [] as IGalleryItem[]
        }
    },
    provide() {
        return {
            dHash: uuidv4()
        }
    },
    computed: {
        ...mapState(mainStore, {
            user: 'user'
        })
    },
    setup() {
        const store = dashboardStore()
        const appStore = mainStore()
        return { store, appStore }
    },
    async created() {
        this.setEventListeners()
        await this.getData()
        this.$watch('model.configuration.datasets', (modelDatasets: IDashboardDataset[]) => {
            setDatasetIntervals(modelDatasets, this.datasets)
        })
    },
    unmounted() {
        this.emptyStoreValues()
        clearAllDatasetIntervals()
    },
    methods: {
        ...mapActions(dashboardStore, ['removeSelections', 'setAllDatasets', 'getSelections', 'setInternationalization', 'getInternationalization', 'setDashboardDocument', 'setDashboardDrivers', 'setProfileAttributes']),
        async getData() {
            this.loading = true

            if (this.filtersData) this.drivers = loadDrivers(this.filtersData, this.model)
            await Promise.all([this.loadProfileAttributes(), this.loadModel(), this.loadInternationalization()])
            this.setDashboardDrivers(this.dashboardId, this.drivers)
            this.loadHtmlGallery()
            this.loadCustomChartGallery()
            this.loading = false
        },
        async loadModel() {
            let tempModel = null as any
            if (this.newDashboardMode) {
                tempModel = createNewDashboardModel()
            } else {
                await this.$http
                    .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/documentexecution/` + this.document?.id + '/templates')
                    .then((response: AxiosResponse<any>) => (tempModel = response.data))
                    .catch(() => {})
            }
            this.datasets = await loadDatasets(tempModel, this.appStore, this.setAllDatasets, this.$http)
            this.model = (tempModel && this.newDashboardMode) || tempModel.hasOwnProperty('id') ? await formatNewModel(tempModel, this.datasets, this.$http) : await (formatModel(tempModel, this.document, this.datasets, this.drivers, this.profileAttributes, this.$http, this.user) as any)
            setDatasetIntervals(this.model?.configuration.datasets, this.datasets)
            this.dashboardId = cryptoRandomString({ length: 16, type: 'base64' })
            this.store.setDashboard(this.dashboardId, this.model)
            this.store.setSelections(this.dashboardId, this.model.configuration.selections, this.$http)
            this.store.setDashboardDocument(this.dashboardId, this.document)
        },
        async loadInternationalization() {
            this.appStore.setLoading(true)
            var result = (this.appStore.$state as any).user.locale.split('_')
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/i18nMessages/?currCountry=${result[1]}&currLanguage=${result[0]}&currScript=`)
                .then((response: AxiosResponse<any>) => {
                    this.internationalization = response.data
                    this.setInternationalization(response.data)
                })
                .catch(() => {})

            this.appStore.setLoading(false)
        },
        async loadCrossNavigations() {
            if (this.newDashboardMode) return
            this.appStore.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/crossNavigation/${this.document?.label}/loadCrossNavigationByDocument`)
                .then((response: AxiosResponse<any>) => (this.crossNavigations = response.data))
                .catch(() => {})
            this.appStore.setLoading(false)
            this.store.setCrossNavigations(this.dashboardId, this.crossNavigations)
        },
        async loadHtmlGallery() {
            await this.$http
                .get(import.meta.env.VITE_API_PATH + `1.0/widgetgallery/widgets/html`)
                .then((response: AxiosResponse<any>) => (this.htmlGallery = response.data))
                .catch(() => {})
        },
        async loadCustomChartGallery() {
            await this.$http
                .get(import.meta.env.VITE_API_PATH + `1.0/widgetgallery/widgets/chart`)
                .then((response: AxiosResponse<any>) => (this.customChartGallery = response.data))
                .catch(() => {})
        },
        loadOutputParameters() {
            if (this.newDashboardMode) return
            // TODO - Remove Mocked Output Parameters
            const mockedParameters = descriptor.mockedOutputParameters
            this.store.setOutputParameters(this.dashboardId, mockedParameters)
        },

        loadProfileAttributes() {
            this.profileAttributes = []
            const user = this.appStore.getUser()
            if (user && user.attributes) {
                Object.keys(user.attributes).forEach((key: string) => this.profileAttributes.push({ name: key, value: user.attributes[key] }))
            }
            this.setProfileAttributes(this.profileAttributes)
        },
        setEventListeners() {
            emitter.on('openNewWidgetPicker', () => {
                this.openNewWidgetPicker()
            })
            emitter.on('openDatasetManagement', () => {
                this.openDatasetManagementDialog()
            })
            emitter.on('openWidgetEditor', (widget) => {
                this.openWidgetEditor(widget as IWidget)
            })
            emitter.on('saveDashboard', () => {
                this.onSaveDashboardClicked()
            })
            emitter.on('openDashboardGeneralSettings', () => {
                this.openGeneralSettings()
            })
        },
        openNewWidgetPicker() {
            this.widgetPickerVisible = true
        },
        openDatasetManagementDialog() {
            this.datasetEditorVisible = true
            clearAllDatasetIntervals()
        },
        openWidgetEditor(widget: IWidget) {
            this.selectedWidget = widget
            this.setWidgetEditorToVisible()
        },
        openNewWidgetEditor(widget: any) {
            this.selectedWidget = { type: widget?.type, new: true }
            this.setWidgetEditorToVisible()
        },
        setWidgetEditorToVisible() {
            this.widgetPickerVisible = false
            this.widgetEditorVisible = true
            emitter.emit('widgetEditorOpened')
            clearAllDatasetIntervals()
        },
        emptyStoreValues() {
            this.store.removeDashboard(this.dashboardId)
            this.store.setCrossNavigations(this.dashboardId, [])
            this.store.setOutputParameters(this.dashboardId, [])
            this.store.setSelections(this.dashboardId, [], this.$http)
            this.store.setSelections(this.dashboardId, [], this.$http)
            this.setDashboardDrivers(this.dashboardId, [])
            this.setProfileAttributes([])
        },
        closeWidgetEditor() {
            this.widgetEditorVisible = false
            this.selectedWidget = null
            emitter.emit('widgetEditorClosed')
            setDatasetIntervals(this.model.configuration.datasets, this.datasets)
        },
        closeDatasetEditor() {
            this.datasetEditorVisible = false
            emitter.emit('datasetManagementClosed')
        },
        async onSaveDashboardClicked() {
            if (!this.document) return
            if (this.newDashboardMode) {
                this.saveDialogVisible = true
            } else {
                await this.saveDashboard(this.document)
            }
        },
        async saveNewDashboard(document: { name: string; label: string }) {
            await this.saveDashboard(document)
        },
        async saveDashboard(document: any) {
            this.appStore.setLoading(true)
            if (!this.document) return
            const folders = this.newDashboardMode && this.$route.query.folderId ? [this.$route.query.folderId] : []
            const postData = {
                document: {
                    name: document.name,
                    label: document.label,
                    description: document.description,
                    type: 'DOCUMENT_COMPOSITE'
                },
                customData: {
                    templateContent: this.getTemplateContent()
                },
                action: this.newDashboardMode ? 'DOC_SAVE' : 'MODIFY_COCKPIT',
                folders: folders
            }

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/saveDocument`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.appStore.setInfo({
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.saveDialogVisible = false
                    if (this.newDashboardMode) this.$emit('newDashboardSaved', document)
                })
                .catch(() => {})

            this.appStore.setLoading(false)
        },
        getTemplateContent() {
            const dashboardModel = deepcopy(this.store.getDashboard(this.dashboardId))
            dashboardModel.configuration.selections = this.getSelections(this.dashboardId)
            formatDashboardForSave(dashboardModel)
            return dashboardModel
        },
        onSelectionsRemove(selections: ISelection[]) {
            this.selectionsDialogVisible = false
            this.removeSelections(selections, this.dashboardId)
        },
        openGeneralSettings() {
            this.generalSettingsVisible = true
        },
        closeGeneralSettings() {
            this.generalSettingsVisible = false
            emitter.emit('dashboardGeneralSettingsClosed')
        }
    }
})
</script>
<style lang="scss">
.dashboard-container {
    flex: 1;
}
@media screen and (max-width: 600px) {
    .dashboard-container {
        height: calc(100vh - var(--kn-mainmenu-width));
    }
}
</style>
