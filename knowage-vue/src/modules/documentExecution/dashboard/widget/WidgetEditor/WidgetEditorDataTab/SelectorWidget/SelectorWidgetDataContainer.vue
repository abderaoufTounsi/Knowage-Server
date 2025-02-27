<template>
    <div v-if="widgetModel">
        <div class="p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.sortingOrder') }}</label>
            <Dropdown class="kn-material-input" v-model="sortingOrder" :options="commonDescriptor.sortingOrderOptions" optionValue="value" @change="sortingChanged">
                <template #value="slotProps">
                    <div>
                        <span>{{ slotProps.value }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
        </div>
        <WidgetEditorColumnTable class="p-m-2" :widgetModel="widgetModel" :items="columnTableItems" :settings="commonDescriptor.columnTableSettings" @itemAdded="onColumnAdded" @itemUpdated="onColumnItemUpdate" @itemSelected="setSelectedColumn" @itemDeleted="onColumnDelete"></WidgetEditorColumnTable>
        <WidgetEditorFilterForm v-if="selectedColumn" :propColumn="selectedColumn"></WidgetEditorFilterForm>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import descriptor from '../TableWidget/TableWidgetDataDescriptor.json'
import Dropdown from 'primevue/dropdown'
import commonDescriptor from '../common/WidgetCommonDescriptor.json'
import WidgetEditorColumnTable from '../common/WidgetEditorColumnTable.vue'
import WidgetEditorFilterForm from '../common/WidgetEditorFilterForm.vue'

export default defineComponent({
    name: 'selector-widget-data-container',
    components: { Dropdown, WidgetEditorColumnTable, WidgetEditorFilterForm },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {
            descriptor,
            commonDescriptor,
            columnTableItems: [] as IWidgetColumn[],
            selectedColumn: null as IWidgetColumn | null,
            sortingOrder: ''
        }
    },
    watch: {
        selectedDataset() {
            this.selectedColumn = null
        }
    },
    async created() {
        this.$watch('widgetModel.columns', () => this.loadColumnTableItems())
        this.loadColumnTableItems()
        this.loadSortingOrder()
    },
    methods: {
        loadColumnTableItems() {
            this.columnTableItems = this.widgetModel.columns ?? []
        },
        loadSortingOrder() {
            this.sortingOrder = this.widgetModel.settings.sortingOrder ?? ''
        },
        onColumnAdded(payload: { column: IWidgetColumn; rows: IWidgetColumn[] }) {
            if (this.widgetModel.columns.length > 0) {
                emitter.emit('columnRemoved', this.widgetModel.columns[0])
                this.selectedColumn = null
            }
            this.widgetModel.settings.isDateType = payload.column.type.toLowerCase().includes('date') || payload.column.type.toLowerCase().includes('timestamp')
            this.widgetModel.columns = [payload.column]
            emitter.emit('columnAdded', payload.column)
            emitter.emit('refreshSelector', this.widgetModel.id)
            emitter.emit('refreshWidgetWithData', this.widgetModel.id)
        },
        onColumnItemUpdate(column: IWidgetColumn) {
            this.widgetModel.columns[0] = { ...column }
            emitter.emit('collumnUpdated', { column: this.widgetModel.columns[0], columnIndex: 0 })
            emitter.emit('refreshSelector', this.widgetModel.id)
            if (this.widgetModel.columns[0].id === this.selectedColumn?.id) this.selectedColumn = { ...this.widgetModel.columns[0] }
            emitter.emit('refreshWidgetWithData', this.widgetModel.id)
        },
        setSelectedColumn(column: IWidgetColumn) {
            this.selectedColumn = { ...column }
        },
        onColumnDelete(column: IWidgetColumn) {
            this.widgetModel.columns = []
            if (column.id === this.selectedColumn?.id) this.selectedColumn = null
            emitter.emit('columnRemoved', column)
            emitter.emit('refreshSelector', this.widgetModel.id)
            if (this.widgetModel.columns.length == 0) emitter.emit('clearWidgetData', this.widgetModel.id)
            else emitter.emit('refreshWidgetWithData', this.widgetModel.id)
        },
        sortingChanged() {
            this.widgetModel.settings.sortingOrder = this.sortingOrder
            if (this.widgetModel.columns.length > 0) emitter.emit('refreshWidgetWithData', this.widgetModel.id)
            emitter.emit('refreshSelector', this.widgetModel.id)
        }
    }
})
</script>
