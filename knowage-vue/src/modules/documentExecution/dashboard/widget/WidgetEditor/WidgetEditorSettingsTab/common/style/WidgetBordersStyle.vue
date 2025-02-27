<template>
    <div v-if="bordersStyleModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-grid p-ai-center">
            <div class="p-col-12 p-md-4 p-d-flex p-flex-column p-px-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.borders.bordersStyle') }}</label>
                <Dropdown class="kn-material-input" v-model="bordersStyleModel.properties['border-style']" :options="descriptor.bordersStyleOptions" optionValue="value" :disabled="bordersStyleDisabled" @change="bordersStyleChanged">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.bordersStyleOptions, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
            </div>
            <div class="p-col-12 p-md-4 p-d-flex p-flex-column p-px-2 p-pt-4">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.borders.bordersThickness') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="bordersStyleModel.properties['border-width']" :disabled="bordersStyleDisabled" @change="bordersStyleChanged" />
                <small>{{ $t('dashboard.widgetEditor.borders.bordersThicknessHint') }}</small>
            </div>
            <div class="p-col-12 p-md-4 p-px-2 p-pt-4">
                <WidgetEditorColorPicker :initialValue="bordersStyleModel.properties['border-width']" :label="$t('dashboard.widgetEditor.borders.bordersColor')" :disabled="bordersStyleDisabled" @change="onSelectionColorChanged"></WidgetEditorColorPicker>
            </div>
        </div>

        <div class="p-col-12 p-grid p-ai-center">
            <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex p-px-2 p-pt-4">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.borders.borderRadiusTopLeft') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="bordersStyleModel.properties['border-top-left-radius']" :disabled="bordersStyleDisabled" @change="bordersStyleChanged" />
                <small>{{ $t('dashboard.widgetEditor.inputHintForPixels') }}</small>
            </div>
            <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex p-px-2 p-pt-4">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.borders.borderRadiusTopRight') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="bordersStyleModel.properties['border-top-right-radius']" :disabled="bordersStyleDisabled" @change="bordersStyleChanged" />
                <small>{{ $t('dashboard.widgetEditor.inputHintForPixels') }}</small>
            </div>
            <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex p-px-2 p-pt-4">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.borders.borderRadiusBottomLeft') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="bordersStyleModel.properties['border-bottom-left-radius']" :disabled="bordersStyleDisabled" @change="bordersStyleChanged" />
                <small>{{ $t('dashboard.widgetEditor.inputHintForPixels') }}</small>
            </div>
            <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex p-px-2 p-pt-4">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.borders.borderRadiusBottomRight') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="bordersStyleModel.properties['border-bottom-right-radius']" :disabled="bordersStyleDisabled" @change="bordersStyleChanged" />
                <small>{{ $t('dashboard.widgetEditor.inputHintForPixels') }}</small>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetBordersStyle } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../../WidgetEditorSettingsTabDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorColorPicker from '../../common/WidgetEditorColorPicker.vue'

export default defineComponent({
    name: 'widget-borders-style',
    components: { Dropdown, InputSwitch, WidgetEditorColorPicker },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true }
    },
    data() {
        return {
            descriptor,
            bordersStyleModel: null as IWidgetBordersStyle | null,
            widgetType: '' as string,
            getTranslatedLabel
        }
    },
    computed: {
        bordersStyleDisabled() {
            return !this.bordersStyleModel || !this.bordersStyleModel.enabled
        }
    },
    created() {
        this.loadBordersStyle()
    },
    methods: {
        loadBordersStyle() {
            if (!this.widgetModel) return
            this.widgetType = this.widgetModel.type
            if (this.widgetModel?.settings?.style?.borders) this.bordersStyleModel = this.widgetModel.settings.style.borders
        },
        bordersStyleChanged() {
            switch (this.widgetType) {
                case 'table':
                    emitter.emit('refreshTable', this.widgetModel.id)
                    break
                case 'selector':
                    emitter.emit('refreshSelector', this.widgetModel.id)
                    break
                case 'selection':
                    emitter.emit('refreshSelection', this.widgetModel.id)
            }
        },
        onSelectionColorChanged(event: string | null) {
            if (!event || !this.bordersStyleModel) return
            this.bordersStyleModel.properties['border-color'] = event
            this.bordersStyleChanged()
        }
    }
})
</script>
