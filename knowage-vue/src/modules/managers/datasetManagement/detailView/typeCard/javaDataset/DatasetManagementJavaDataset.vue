<template>
    <Card class="p-m-2">
        <template #content>
            <div class="p-field">
                <span class="p-float-label">
                    <InputText
                        id="jClassName"
                        class="kn-material-input"
                        type="text"
                        :style="javaDatasetDescriptor.style.maxWidth"
                        v-model.trim="v$.dataset.jClassName.$model"
                        :class="{
                            'p-invalid': v$.dataset.jClassName.$invalid && v$.dataset.jClassName.$dirty
                        }"
                        @blur="v$.dataset.jClassName.$touch()"
                        @change="$emit('touched')"
                    />
                    <label for="jClassName" class="kn-material-input-label"> {{ $t('managers.lovsManagement.javaClassName') }} * </label>
                </span>
                <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.jClassName" :additionalTranslateParams="{ fieldName: $t('managers.lovsManagement.javaClassName') }" />
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import javaDatasetDescriptor from './DatasetManagementJavaDatasetDescriptor.json'
import Card from 'primevue/card'
import mainStore from '../../../../../../App.store'

export default defineComponent({
    components: { Card, KnValidationMessages },
    props: { selectedDataset: { type: Object as any } },
    emits: ['touched'],
    data() {
        return {
            dataset: {} as any,
            javaDatasetDescriptor,
            v$: useValidate() as any
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.dataset = this.selectedDataset
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
        }
    },
    validations() {
        const javaClassFieldRequired = (value) => {
            return this.dataset.dsTypeCd != 'Java Class' || value
        }
        const customValidators: ICustomValidatorMap = { 'java-class-field-required': javaClassFieldRequired }
        const validationObject = { dataset: createValidations('dataset', javaDatasetDescriptor.validations.dataset, customValidators) }
        return validationObject
    },
    methods: {
        changeTypeWarning() {
            this.store.setInfo({ title: this.$t('documentExecution.registry.warning'), msg: this.$t('managers.datasetManagement.changeTypeMsg') })
        }
    }
})
</script>
