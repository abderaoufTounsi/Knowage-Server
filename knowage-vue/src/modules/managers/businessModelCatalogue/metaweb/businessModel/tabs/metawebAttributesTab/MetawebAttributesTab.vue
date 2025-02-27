<template>
    <div>
        <DataTable v-if="businessModel" class="p-datatable-sm kn-table p-m-2" :value="businessModel.columns" :loading="loading" responsiveLayout="stack" breakpoint="960px" @rowReorder="onRowReorder">
            <Column :rowReorder="true" :headerStyle="metawebAttributesTabDescriptor.reorderColumnStyle" :reorderableColumn="false" />
            <Column class="kn-truncated" v-for="(column, index) in metawebAttributesTabDescriptor.columns" :key="index" :field="column.field" :header="$t(column.header)">
                <template #body="slotProps">
                    <div class="p-d-flex p-flex-row">
                        <Checkbox v-if="column.field === 'identifier'" v-model="slotProps.data[slotProps.column.props.field]" :binary="true" @change="$emit('metaUpdated')"></Checkbox>
                        <Checkbox v-else-if="column.field === 'visible'" v-model="columnsVisibility[slotProps.data.uniqueName]" :binary="true" @change="onChange(slotProps.data, 'visibility')"></Checkbox>
                        <Checkbox v-else-if="column.field === 'personal'" v-model="columnsPersonal[slotProps.data.uniqueName]" :binary="true" @change="onChange(slotProps.data, 'personal')"></Checkbox>
                        <Checkbox v-else-if="column.field === 'decrypt'" v-model="columnsDecrypt[slotProps.data.uniqueName]" :binary="true" @change="onChange(slotProps.data, 'decrypt')"></Checkbox>
                        <Checkbox v-else-if="column.field === 'subjectId'" v-model="columnsSubjectId[slotProps.data.uniqueName]" :binary="true" @change="onChange(slotProps.data, 'subjectId')"></Checkbox>
                        <Dropdown v-else-if="column.field === 'type'" class="kn-material-input" v-model="columnsType[slotProps.data.uniqueName]" :options="metawebAttributesTabDescriptor.typeOptions" @change="onChange(slotProps.data, 'type')" />
                        <InputText v-else-if="column.field === 'name'" v-model="slotProps.data[slotProps.column.props.field]" class="kn-material-input p-inputtext-sm p-p-2" @blur="$emit('metaUpdated')" />
                        <span v-else>{{ slotProps.data[slotProps.column.props.field] }}</span>
                    </div>
                </template>
            </Column>
            <Column :style="metawebAttributesTabDescriptor.iconColumnStyle">
                <template #header>
                    <Button class="kn-button kn-button--primary p-button-link p-jc-center" @click="openUnusedFieldsDialog" data-test="add-button"> {{ $t('common.add') }}</Button>
                </template>

                <template #body="slotProps">
                    <div class="p-d-flex p-flex-row p-jc-end">
                        <Button icon="far fa-edit" class="p-button-link" @click="openAttributeDialog(slotProps.data)" :data-test="'open-icon-' + slotProps.data.name" />
                        <Button icon="pi pi-trash" class="p-button-link" @click="deleteBusinessColumnConfirm(slotProps.data)" :data-test="'delete-icon-' + slotProps.data.name" />
                    </div>
                </template>
            </Column>
        </DataTable>

        <MetawebAttributeDetailDialog :visible="attributeDetailDialogVisible" :selectedAttribute="selectedAttribute" :roles="roles" @close="attributeDetailDialogVisible = false" @save="onAttributeSave"></MetawebAttributeDetailDialog>
        <MetawebAttributeUnusedFieldDialog :visible="unusedFieldDialogVisible" :unusedFields="unusedFields" @close="unusedFieldDialogVisible = false" @save="saveUnusedFields"></MetawebAttributeUnusedFieldDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { iBusinessModel, iBusinessModelColumn } from '../../../Metaweb'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import metawebAttributesTabDescriptor from './MetawebAttributesTabDescriptor.json'
import MetawebAttributeDetailDialog from './dialogs/metawebAttributeDetail/MetawebAttributeDetailDialog.vue'
import MetawebAttributeUnusedFieldDialog from './dialogs/metawebAttributeUnusedField/MetawebAttributeUnusedFieldDialog.vue'
import mainStore from '../../../../../../../App.store'
import { generate, applyPatch } from 'fast-json-patch'

export default defineComponent({
    name: 'metaweb-attributes-tab',
    components: { Checkbox, Column, DataTable, Dropdown, MetawebAttributeDetailDialog, MetawebAttributeUnusedFieldDialog },
    props: { selectedBusinessModel: { type: Object as PropType<iBusinessModel | null> }, propMeta: { type: Object }, observer: { type: Object }, roles: { type: Array } },
    emits: ['loading', 'metaUpdated'],
    data() {
        return {
            metawebAttributesTabDescriptor,
            meta: null as any,
            businessModel: null as iBusinessModel | null,
            columnsVisibility: {} as any,
            columnsType: {} as any,
            columnsPersonal: {} as any,
            columnsDecrypt: {} as any,
            columnsSubjectId: {} as any,
            attributeDetailDialogVisible: false,
            selectedAttribute: null as iBusinessModelColumn | null,
            unusedFieldDialogVisible: false,
            unusedFields: [] as any[],
            metaObserver: null as any,
            propertyKeys: [] as string[],
            loading: false
        }
    },
    watch: {
        selectedBusinessModel() {
            this.loadMeta()
            this.loadBusinessModel()
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.loadMeta()
        this.loadBusinessModel()
    },
    methods: {
        loadMeta() {
            this.meta = this.propMeta as any
        },
        loadBusinessModel() {
            this.businessModel = this.selectedBusinessModel as iBusinessModel

            this.formatBusinessModel()
        },
        formatBusinessModel() {
            if (this.businessModel) {
                this.businessModel.columns?.forEach((column: any) => {
                    for (let i = 0; i < column.properties.length; i++) {
                        const tempProperty = column.properties[i]
                        const key = Object.keys(tempProperty)[0]

                        if (!this.propertyKeys.includes(key)) this.propertyKeys.push(key)

                        if (key === 'structural.visible') {
                            this.columnsVisibility[column.uniqueName] = tempProperty[key].value === true || tempProperty[key].value === 'true'
                        } else if (key === 'structural.columntype') {
                            this.columnsType[column.uniqueName] = tempProperty[key].value
                        } else if (key === 'structural.personal') {
                            this.columnsPersonal[column.uniqueName] = tempProperty[key].value === true || tempProperty[key].value === 'true'
                        } else if (key === 'structural.decrypt') {
                            this.columnsDecrypt[column.uniqueName] = tempProperty[key].value === true || tempProperty[key].value === 'true'
                        } else if (key === 'structural.subjectId') {
                            this.columnsSubjectId[column.uniqueName] = tempProperty[key].value === true || tempProperty[key].value === 'true'
                        }
                    }
                })
            }
        },
        async onRowReorder(event: any) {
            this.loading = true
            const postData = { data: { businessModelUniqueName: this.businessModel?.uniqueName, index: event.dragIndex, direction: event.dropIndex - event.dragIndex }, diff: generate(this.observer) }
            await this.$http
                .post(import.meta.env.VITE_META_API_URL + `/1.0/metaWeb/moveBusinessColumn`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.meta = applyPatch(this.meta, response.data).newDocument
                })
                .catch(() => {})
                .finally(() => generate(this.observer))
            this.loading = false
        },
        onChange(column: iBusinessModelColumn, type: string) {
            for (let i = 0; i < column.properties.length; i++) {
                const tempProperty = column.properties[i]
                const key = Object.keys(tempProperty)[0]
                if (key === 'structural.visible' && type === 'visibility') {
                    tempProperty[key].value = this.columnsVisibility[column.uniqueName]
                } else if (key === 'structural.columntype' && type === 'type') {
                    tempProperty[key].value = this.columnsType[column.uniqueName]
                } else if (key === 'structural.personal' && type === 'personal') {
                    tempProperty[key].value = this.columnsPersonal[column.uniqueName]
                } else if (key === 'structural.decrypt' && type === 'decrypt') {
                    tempProperty[key].value = this.columnsDecrypt[column.uniqueName]
                } else if (key === 'structural.subjectId' && type === 'subjectId') {
                    tempProperty[key].value = this.columnsSubjectId[column.uniqueName]
                }
            }

            this.$emit('metaUpdated')
        },
        openAttributeDialog(attribute: iBusinessModelColumn) {
            this.selectedAttribute = attribute
            this.attributeDetailDialogVisible = true
        },
        onAttributeSave(attribute: iBusinessModelColumn) {
            this.selectedAttribute = attribute

            if (this.businessModel) {
                const index = this.businessModel.columns.findIndex((el: iBusinessModelColumn) => el.uniqueName === this.selectedAttribute?.uniqueName)
                if (index !== -1) {
                    this.businessModel.columns[index] = this.selectedAttribute
                }
            }

            this.formatBusinessModel()
            this.$emit('metaUpdated')
            this.attributeDetailDialogVisible = false
        },
        deleteBusinessColumnConfirm(attribute: iBusinessModelColumn) {
            this.$confirm.require({
                message: this.$t('documentExecution.dossier.deleteConfirm'),
                header: this.$t('documentExecution.dossier.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => await this.deleteBusinessColumn(attribute)
            })
        },
        async deleteBusinessColumn(attribute: iBusinessModelColumn) {
            this.loading = true
            const postData = { data: { businessColumnUniqueName: attribute.uniqueName, businessModelUniqueName: this.businessModel?.uniqueName }, diff: generate(this.observer) }

            await this.$http
                .post(import.meta.env.VITE_META_API_URL + `/1.0/metaWeb/deleteBusinessColumn`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.meta = applyPatch(this.meta, response.data).newDocument

                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    generate(this.observer)
                })
                .catch(() => {})
            this.loading = false
        },
        openUnusedFieldsDialog() {
            if (this.businessModel && this.businessModel.physicalTable) {
                this.unusedFields = []
                const physicalTable = this.meta?.physicalModels[this.businessModel?.physicalTable.physicalTableIndex]
                const allColumns = [...physicalTable.columns]

                for (let i = 0; i < allColumns.length; i++) {
                    const tempColumn = allColumns[i]

                    if (tempColumn.markedDeleted) {
                        continue
                    } else {
                        const index = this.businessModel.columns.findIndex((el: any) => el.uniqueName === tempColumn.name)
                        if (index === -1) this.unusedFields.push(tempColumn)
                    }
                }
            }

            this.unusedFieldDialogVisible = true
        },
        async saveUnusedFields(unusedColumns: any[]) {
            this.loading = true
            const tempColumns = unusedColumns.map((el: any) => {
                return { businessModelUniqueName: this.businessModel?.uniqueName, physicalColumnName: el.name, physicalTableName: el.tableName }
            })
            const postData = { data: { columns: tempColumns }, diff: generate(this.observer) }
            await this.$http
                .post(import.meta.env.VITE_META_API_URL + `/1.0/metaWeb/createBusinessColumn`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.meta = applyPatch(this.meta, response.data).newDocument
                    this.unusedFieldDialogVisible = false
                })
                .catch(() => {})
                .finally(() => generate(this.observer))
            this.loading = false
        }
    }
})
</script>
