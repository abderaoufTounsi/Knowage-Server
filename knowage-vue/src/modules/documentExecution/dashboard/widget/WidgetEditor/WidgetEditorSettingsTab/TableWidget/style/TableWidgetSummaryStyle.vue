<template>
    <div v-if="summaryStyleModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12">
            <WidgetEditorStyleToolbar :options="settingsDescriptor.defaultToolbarStyleOptions" :propModel="summaryStyleModel" :disabled="summaryStyleDisabled" @change="onStyleToolbarChange($event)"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetSummaryStyle, IWidgetStyleToolbarModel } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import settingsDescriptor from '../../WidgetEditorSettingsTabDescriptor.json'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'table-widget-summary-style',
    components: { WidgetEditorStyleToolbar },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true }
    },
    data() {
        return {
            settingsDescriptor,
            summaryStyleModel: null as ITableWidgetSummaryStyle | null
        }
    },
    computed: {
        summaryStyleDisabled() {
            return !this.widgetModel.settings?.configuration?.summaryRows?.enabled
        }
    },
    created() {
        this.loadSummaryRowsStyle()
    },
    methods: {
        loadSummaryRowsStyle() {
            if (this.widgetModel?.settings?.style?.summary) this.summaryStyleModel = this.widgetModel.settings.style.summary
        },
        summaryStyleChanged() {
            emitter.emit('summaryStyleChanged', this.summaryStyleModel)
            emitter.emit('refreshTable', this.widgetModel.id)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            this.summaryStyleModel = {
                'background-color': model['background-color'] ?? '',
                color: model.color ?? '',
                'justify-content': model['justify-content'] ?? '',
                'font-size': model['font-size'] ?? '14px',
                'font-family': model['font-family'] ?? '',
                'font-style': model['font-style'] ?? '',
                'font-weight': model['font-weight'] ?? ''
            }
            this.widgetModel.settings.style.summary = this.summaryStyleModel
            this.summaryStyleChanged()
        }
    }
})
</script>

<style lang="scss" scoped>
#height-input-container {
    max-width: 200px;
}
</style>
