<template>
    <div class="p-d-flex p-flex-row p-ai-center">
        <InputSwitch v-if="model" class="p-mr-3" v-model="model.enabled" @click.stop="() => {}"></InputSwitch>
        <label class="kn-material-input-label">{{ title ? $t(title) : '' }}</label>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'text-widget-settings-accordion-header',
    components: { Checkbox, Dropdown, InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, title: { type: String }, type: { type: String, required: true } },
    data() {
        return {
            model: null as any
        }
    },
    computed: {},
    watch: {
        type() {
            this.model = this.loadModel()
        }
    },
    created() {
        this.model = this.loadModel()
    },
    methods: {
        loadModel() {
            if (!this.widgetModel || !this.widgetModel.settings) return null
            switch (this.type) {
                case 'Title':
                    return this.widgetModel.settings.style.title
                case 'LabelStyle':
                    return this.widgetModel.settings.style.label
                case 'BackgroundColorStyle':
                    return this.widgetModel.settings.style.background
                case 'BordersStyle':
                    return this.widgetModel.settings.style.borders
                case 'PaddingStyle':
                    return this.widgetModel.settings.style.padding
                case 'ShadowsStyle':
                    return this.widgetModel.settings.style.shadows
                case 'CrossNavigation':
                    return this.widgetModel.settings.interactions.crossNavigation
                case 'Preview':
                    return this.widgetModel.settings.interactions.preview
                default:
                    return null
            }
        }
    }
})
</script>
