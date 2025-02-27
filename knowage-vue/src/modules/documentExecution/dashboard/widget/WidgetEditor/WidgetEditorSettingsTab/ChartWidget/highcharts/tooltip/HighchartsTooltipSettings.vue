<template>
    <div v-if="model" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="descriptor.tooltipStyleOptions" :propModel="toolbarModel" :disabled="tooltipDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
        <div class="p-col-12 p-py-4">
            <div class="p-d-flex p-flex-row p-jc-center">
                <label class="kn-material-input-label kn-cursor-pointer" @click="advancedVisible = !advancedVisible">{{ $t('common.advanced') }}<i :class="advancedVisible ? 'fas fa-chevron-up' : 'fas fa-chevron-down'" class="p-ml-2"></i></label>
            </div>
            <Transition>
                <div v-if="advancedVisible" class="p-d-flex p-flex-column">
                    <div class="p-col-12">
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.formatter') }}</label>
                        <Message v-if="model.tooltip.formatterError" class="p-m-2" severity="warn" :closable="false" :style="descriptor.warningMessageStyle">
                            {{ model.tooltip.formatterError }}
                        </Message>
                        <div class="p-d-flex p-flex-row p-ai-center">
                            <HighchartsFormatterCodeMirror :propCode="model.tooltip.formatterText" @change="onFormatterChange($event, 'formatter')" @blur="modelChanged"></HighchartsFormatterCodeMirror>
                            <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.tooltip.formatterHint')"></i>
                        </div>
                    </div>
                    <div class="p-col-12">
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.highcharts.tooltip.pointFormatter') }}</label>
                        <Message v-if="model.tooltip.pointFormatterError" class="p-m-2" severity="warn" :closable="false" :style="descriptor.warningMessageStyle">
                            {{ model.tooltip.pointFormatterError }}
                        </Message>
                        <div class="p-d-flex p-flex-row p-ai-center">
                            <HighchartsFormatterCodeMirror :propCode="model.tooltip.pointFormatterText" @change="onFormatterChange($event, 'pointFormatter')" @blur="modelChanged"></HighchartsFormatterCodeMirror>
                            <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.tooltip.pointFormatterHint')"></i>
                        </div>
                    </div>
                </div>
            </Transition>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import descriptor from '../HighchartsWidgetSettingsDescriptor.json'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import Message from 'primevue/message'
import WidgetEditorStyleToolbar from '../../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import HighchartsFormatterCodeMirror from '../common/HighchartsFormatterCodeMirror.vue'

export default defineComponent({
    name: 'hihgcharts-tooltip-settings',
    components: {
        InputSwitch,
        InputNumber,
        Message,
        WidgetEditorStyleToolbar,
        HighchartsFormatterCodeMirror
    },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as IHighchartsChartModel | null,
            toolbarModel: {} as {
                'font-family': string
                'font-size': string
                'font-weight': string
                color: string
                'background-color': string
            },
            advancedVisible: false
        }
    },
    computed: {
        tooltipDisabled(): boolean {
            return !this.model || !this.model.tooltip.enabled
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            if (this.model?.tooltip)
                this.toolbarModel = {
                    'font-family': this.model.tooltip.style.fontFamily,
                    'font-size': this.model.tooltip.style.fontSize,
                    'font-weight': this.model.tooltip.style.fontWeight,
                    color: this.model.tooltip.style.color,
                    'background-color': this.model.tooltip.backgroundColor
                }
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.model || !this.model.tooltip) return
            this.toolbarModel = {
                'font-family': model['font-family'] ?? '',
                'font-size': model['font-size'] ?? '',
                'font-weight': model['font-weight'] ?? '',
                color: model.color ?? '',
                'background-color': model['background-color'] ?? ''
            }
            ;(this.model.tooltip.backgroundColor = this.toolbarModel['background-color'] ?? ''),
                (this.model.tooltip.style = {
                    fontFamily: this.toolbarModel['font-family'] ?? '',
                    fontSize: this.toolbarModel['font-size'] ?? '14px',
                    fontWeight: this.toolbarModel['font-weight'] ?? '',
                    color: this.toolbarModel.color ?? ''
                })
            this.modelChanged()
        },
        onFormatterChange(newValue: string, type: 'formatter' | 'pointFormatter') {
            if (!this.model) return
            if (type === 'formatter') this.model.tooltip.formatterText = newValue
            else this.model.tooltip.pointFormatterText = newValue
        }
    }
})
</script>

<style lang="scss" scoped>
.v-enter-active,
.v-leave-active {
    transition: opacity 0.3s ease;
}

.v-enter-from,
.v-leave-to {
    opacity: 0;
}
</style>
