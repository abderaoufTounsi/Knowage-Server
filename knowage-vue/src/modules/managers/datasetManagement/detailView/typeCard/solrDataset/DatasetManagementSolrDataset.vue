<template>
    <Card class="p-m-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText
                            id="restAddress"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.dataset.restAddress.$model"
                            :class="{
                                'p-invalid': v$.dataset.restAddress.$invalid && v$.dataset.restAddress.$dirty
                            }"
                            @blur="v$.dataset.restAddress.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="restAddress" class="kn-material-input-label"> {{ $t('managers.datasetManagement.restAddress') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.restAddress" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.restAddress') }" />
                </div>
                <div class="p-field p-col-6 p-float-label">
                    <span class="p-float-label">
                        <InputText
                            id="solrCollection"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.dataset.solrCollection.$model"
                            :class="{
                                'p-invalid': v$.dataset.solrCollection.$invalid && v$.dataset.solrCollection.$dirty
                            }"
                            @blur="v$.dataset.solrCollection.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="solrCollection" class="kn-material-input-label"> {{ $t('managers.datasetManagement.solrCollection') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.solrCollection" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.solrCollection') }" />
                </div>
                <div class="p-field p-col-12 p-float-label">
                    <InputText id="restRequestBody" class="kn-material-input" type="text" maxLength="2000" v-model.trim="dataset.restRequestBody" @change="$emit('touched')" />
                    <label for="restRequestBody" class="kn-material-input-label"> {{ $t('kpi.measureDefinition.query') }} </label>
                </div>
                <div class="p-field p-col-12 p-float-label" v-if="dataset.solrType == 'DOCUMENTS'">
                    <InputText
                        id="solrFieldList"
                        class="kn-material-input"
                        type="text"
                        v-model.trim="v$.dataset.solrFieldList.$model"
                        :class="{
                            'p-invalid': v$.dataset.solrFieldList.$invalid && v$.dataset.solrFieldList.$dirty
                        }"
                        @blur="v$.dataset.solrFieldList.$touch()"
                        @change="$emit('touched')"
                    />
                    <label for="solrFieldList" class="kn-material-input-label"> {{ $t('managers.datasetManagement.solrFieldList') }} * </label>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.solrFieldList" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.solrFieldList') }" />
                </div>
            </form>
        </template>
    </Card>

    <RequestHeadersTable :selectedDataset="selectedDataset" />
    <QueryParamTable :selectedDataset="selectedDataset" />
    <FacetInfoDialog :visible="facetQueryHelpVisible" @close="facetQueryHelpVisible = false" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import solrDescriptor from './DatasetManagementSolrDatasetDescriptor.json'
import restDescriptor from '../restDataset/DatasetManagementRestDatasetDescriptor.json'
import FacetInfoDialog from '../infoDialogs/DatasetManagementFacetInfoDialog.vue'
import RequestHeadersTable from '../tables/DatasetManagementRequestHeadersTable.vue'
import QueryParamTable from '../tables/DatasetManagementQueryParamTable.vue'
import Card from 'primevue/card'

export default defineComponent({
    components: { Card, KnValidationMessages, FacetInfoDialog, RequestHeadersTable, QueryParamTable },
    props: {
        parentValid: { type: Boolean },
        selectedDataset: { type: Object as any },
        dataSources: { type: Array as any },
        businessModels: { type: Array as any }
    },
    emits: ['touched'],
    data() {
        return {
            restDescriptor,
            solrDescriptor,
            dataset: {} as any,
            v$: useValidate() as any,
            facetQueryHelpVisible: false
        }
    },
    created() {
        this.dataset = this.selectedDataset
        if (this.dataset.dsTypeCd === 'Solr' && (!this.dataset.solrType || this.dataset.solrType === 'FACETS')) this.dataset.solrType = 'DOCUMENTS'
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
        }
    },
    validations() {
        const solrFieldsRequired = (value) => {
            return this.dataset.dsTypeCd != 'Solr' || value
        }
        const documentFieldsRequired = (value) => {
            return this.dataset.solrType != 'DOCUMENTS' || value
        }
        const customValidators: ICustomValidatorMap = { 'solr-fields-required': solrFieldsRequired, 'document-fields-required': documentFieldsRequired }
        const validationObject = { dataset: createValidations('dataset', solrDescriptor.validations.dataset, customValidators) }
        return validationObject
    },
    methods: {}
})
</script>
