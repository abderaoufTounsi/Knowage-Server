<template>
    <div v-if="headersStyleModel" class="p-grid p-ai-center p-p-4">
        <div id="height-input-container" class="p-col-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.height') }}</label>
            <InputNumber class="kn-material-input p-inputtext-sm" v-model="headersStyleModel.height" @blur="headersStyleChanged" />
        </div>

        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="settingsDescriptor.defaultToolbarStyleOptions" :propModel="headersStyleModel.properties" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITawbleWidgetHeadersStyle, IWidgetStyleToolbarModel } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import settingsDescriptor from '../../WidgetEditorSettingsTabDescriptor.json'
import InputNumber from 'primevue/inputnumber'
import WidgetEditorStyleToolbar from '../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
export default defineComponent({
    name: 'table-widget-headers',
    components: { InputNumber, WidgetEditorStyleToolbar },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true }
    },
    data() {
        return {
            settingsDescriptor,
            headersStyleModel: null as ITawbleWidgetHeadersStyle | null
        }
    },
    created() {
        this.loadHeaderModel()
    },
    methods: {
        loadHeaderModel() {
            if (this.widgetModel?.settings?.style?.headers) this.headersStyleModel = this.widgetModel.settings.style.headers
        },
        headersStyleChanged() {
            setTimeout(() => {
                emitter.emit('headersStyleChanged', this.headersStyleModel)
                emitter.emit('refreshTable', this.widgetModel.id)
            }, 0)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.headersStyleModel) return
            this.headersStyleModel.properties = {
                'background-color': model['background-color'] ?? 'rgb(137, 158, 175)',
                color: model.color ?? 'rgb(255, 255, 255)',
                'justify-content': model['justify-content'] ?? 'center',
                'font-size': model['font-size'] ?? '14px',
                'font-family': model['font-family'] ?? '',
                'font-style': model['font-style'] ?? 'normal',
                'font-weight': model['font-weight'] ?? ''
            }
            this.headersStyleChanged()
        }
    }
})
</script>

<style lang="scss" scoped>
#height-input-container {
    max-width: 200px;
}
</style>
