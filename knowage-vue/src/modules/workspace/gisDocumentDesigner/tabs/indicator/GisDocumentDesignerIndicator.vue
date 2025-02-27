<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
        <template #start>{{ $t('workspace.gis.indicators') }}</template>
        <template #end> <Button class="p-button-link" :label="$t('workspace.gis.dsj.addIndicator')" @click="addIndicatorRow" /> </template>
    </Toolbar>
    <div id="informations-content" class="kn-flex kn-relative kn-height-full">
        <div :style="styleDescriptor.style.absoluteScroll">
            <Card>
                <template #content>
                    <DataTable class="p-datatable-sm kn-table georef-step1-table" :value="documentDataProp.indicators" responsiveLayout="scroll" breakpoint="600px">
                        <template #empty>
                            {{ $t('workspace.gis.dnl.emptyInfo') }}
                        </template>
                        <Column field="name" :header="$t('qbe.entities.types.measure')" :sortable="true">
                            <template #body="slotProps">
                                <Dropdown id="measure" class="kn-material-input kn-width-full" v-model="slotProps.data.name" :options="documentDataProp.datasetMeasures" optionLabel="id" optionValue="id" :class="{ 'p-invalid': slotProps.data.name == null }" />
                                <small for="measure" v-if="slotProps.data.name == null" class="p-error">{{ $t('workspace.gis.fieldRequired') }} *</small>
                            </template>
                        </Column>
                        <Column field="label" :header="$t('common.label')" :sortable="true">
                            <template #body="slotProps">
                                <InputText id="label" class="kn-material-input kn-width-full" v-model="slotProps.data.label" :class="{ 'p-invalid': slotProps.data.label == null || slotProps.data.label == '' }" />
                                <small for="label" v-if="slotProps.data.label == null || slotProps.data.label == ''" class="p-error">{{ $t('workspace.gis.fieldRequired') }} *</small>
                            </template>
                        </Column>
                        <Column :style="styleDescriptor.style.trashColumn">
                            <template #body="slotProps">
                                <Button icon="pi pi-trash" class="p-button-link" @click="deleteIndicatorRow(slotProps)" />
                            </template>
                        </Column>
                    </DataTable>
                </template>
            </Card>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import styleDescriptor from '@/modules/workspace/gisDocumentDesigner/GisDocumentDesignerDescriptor.json'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    components: { DataTable, Column, Dropdown },
    emits: ['indicatorsValidationChanged'],
    props: { documentDataProp: { type: Object as any, required: false } },
    computed: {
        indicatorsInvalid() {
            if ((this.documentDataProp.indicators.length == 0 && this.documentDataProp.datasetLabel != '') || this.indicatorsContainEmptyFields) {
                return true
            } else return false
        },
        indicatorsContainEmptyFields() {
            let value = false
            this.documentDataProp.indicators.forEach((field) => {
                if (!field.name || !field.label || field.label === '') {
                    value = true
                } else value = false
            })
            return value
        }
    },
    data() {
        return {
            styleDescriptor,
            documentData: {} as any
        }
    },
    created() {
        this.documentData = this.documentDataProp
        this.$emit('indicatorsValidationChanged', 'indicatorsInvalid', this.indicatorsInvalid)
    },
    watch: {
        documentDataProp() {
            this.documentData = this.documentDataProp
        },
        indicatorsInvalid() {
            this.$emit('indicatorsValidationChanged', 'indicatorsInvalid', this.indicatorsInvalid)
        }
    },
    methods: {
        addIndicatorRow() {
            this.documentData.indicators.push({ name: null, label: null })
        },
        deleteIndicatorRow(eventData) {
            this.documentData.indicators.splice(eventData.index, 1)
        }
    }
})
</script>
