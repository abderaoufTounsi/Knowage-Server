<template>
    <div class="p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow" :style="descriptor.style.preview">
        <DataTable :value="rows" class="p-datatable-sm kn-table" :style="descriptor.style.previewTable" stripedRows rowHover>
            <Column v-for="col of columns" :field="col.name" :header="col.header" :key="col.dataIndex" class="kn-truncated" />
        </DataTable>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the dataset editing preview.
 */
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import mock from './DatasetEditorTestMocks.json'
import descriptor from './DatasetEditorDescriptor.json'

export default defineComponent({
    name: 'dataset-editor-preview',
    components: { Column, DataTable },
    props: {
        selectedDatasetProp: { required: true, type: Object }
    },
    data() {
        return {
            descriptor,
            mock,
            columns: [] as any,
            rows: [] as any
        }
    },
    created() {
        this.setDatatableData()
    },
    methods: {
        setDatatableData() {
            this.mock.previewMock.metaData.fields.forEach((el: any) => {
                typeof el != 'object' ? '' : this.columns.push(el)
            })
            this.rows = this.mock.previewMock.rows
        }
    }
})
</script>
