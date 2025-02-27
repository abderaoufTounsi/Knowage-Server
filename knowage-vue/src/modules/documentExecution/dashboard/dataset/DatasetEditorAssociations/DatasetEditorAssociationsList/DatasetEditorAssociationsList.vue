<template>
    <div class="dashboard-editor-list-card-container p-m-3">
        <div class="dashboard-editor-list-card">
            <div class="p-d-flex p-m-2" :style="associationListDescriptor.style.buttonContainer">
                <Button :label="$t('dashboard.datasetEditor.addIndexes')" class="p-button-outlined kn-flex p-mr-1" @click="$emit('addIndexesOnAssociations')"></Button>
                <Button :label="$t('dashboard.datasetEditor.addAssociation')" icon="pi pi-plus-circle" class="p-button-outlined" @click="$emit('createNewAssociation')"></Button>
            </div>
            <Listbox
                class="kn-list kn-list-no-border-right dashboard-editor-list"
                v-model="selectedAssociation"
                :options="dashboardAssociationsProp"
                :filter="true"
                :filterPlaceholder="$t('common.search')"
                optionLabel="label"
                filterMatchMode="contains"
                :filterFields="['label']"
                :emptyFilterMessage="$t('common.info.noDataFound')"
                @change="selectAssociation"
            >
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item" :style="associationListDescriptor.style.list.listItem" v-tooltip.right="slotProps.option.fields.length">
                        <i v-if="slotProps.option.fields.length === 0" class="fa-solid fa-circle-exclamation p-ml-1 details-warning-color" />
                        <!-- <div v-for="(field, index) of slotProps.option.fields" :key="index">
                            {{ field.column }}
                            <i class="fa-solid fa-arrows-left-right p-mr-1" v-if="index != slotProps.option.fields.length - 1" />
                        </div> -->
                        <span class="p-mx-2">{{ $t('dashboard.datasetEditor.association') }} - {{ slotProps.index }}</span>
                        <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain p-ml-auto" @click.stop="deleteAssociation(slotProps.option.id)" />
                    </div>
                </template>
            </Listbox>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IAssociation } from '../../../Dashboard'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'
import dashStore from '../../../Dashboard.store'
import associationListDescriptor from './DatasetEditorAssociationsListDescriptor.json'

export default defineComponent({
    name: 'dataset-editor-data-list',
    components: { Card, Listbox },
    props: { dashboardAssociationsProp: { required: true, type: Array as any }, selectedAssociationProp: { required: true, type: Object as any } },
    emits: ['createNewAssociation', 'associationSelected', 'associationDeleted', 'addIndexesOnAssociations'],
    data() {
        return {
            associationListDescriptor,
            selectedAssociation: {} as IAssociation
        }
    },
    watch: {
        selectedAssociationProp() {
            this.selectedAssociation = this.selectedAssociationProp
        }
    },
    setup() {
        const dashboardStore = dashStore()
        return { dashboardStore }
    },
    created() {},
    methods: {
        selectAssociation(event) {
            this.$emit('associationSelected', event.value)
        },
        deleteAssociation(associationId) {
            this.$emit('associationDeleted', associationId)
        }
    }
})
</script>
