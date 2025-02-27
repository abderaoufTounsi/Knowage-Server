<template>
    <div v-show="widgetModel">
        <Accordion class="selectorAccordion" v-model:activeIndex="activeIndex">
            <AccordionTab v-for="(accordion, index) in settings" :key="index" :disabled="accordionIsDisabled(accordion.type)">
                <template #header>
                    <SelectionsWidgetSettingsAccordionHeader :widgetModel="widgetModel" :title="accordion.title" :type="accordion.type"></SelectionsWidgetSettingsAccordionHeader>
                </template>
                <SelectorWidgetType v-if="accordion.type === 'SelectorType'" :widgetModel="widgetModel"></SelectorWidgetType>
                <SelectionsWidgetValuesManagement v-if="accordion.type === 'ValuesManagement'" :widgetModel="widgetModel"></SelectionsWidgetValuesManagement>
                <SelectionsNoSelectionConfiguration v-else-if="accordion.type === 'NoSelections'" :widgetModel="widgetModel"></SelectionsNoSelectionConfiguration>
                <WidgetExport v-else-if="accordion.type === 'Export'" :widgetModel="widgetModel"></WidgetExport>
                <WidgetTitleStyle v-else-if="accordion.type === 'Title'" :widgetModel="widgetModel" :toolbarStyleSettings="settingsTabDescriptor.defaultToolbarStyleOptions"></WidgetTitleStyle>
                <SelectionsWidgetChipsStyle v-else-if="accordion.type === 'ChipsStyle'" :widgetModel="widgetModel"></SelectionsWidgetChipsStyle>
                <WidgetRowsStyle v-else-if="accordion.type === 'RowsStyle'" :widgetModel="widgetModel"></WidgetRowsStyle>
                <WidgetBackgroundColorStyle v-else-if="accordion.type === 'BackgroundColorStyle'" :widgetModel="widgetModel"></WidgetBackgroundColorStyle>
                <WidgetPaddingStyle v-else-if="accordion.type === 'PaddingStyle'" :widgetModel="widgetModel"></WidgetPaddingStyle>
                <WidgetBordersStyle v-else-if="accordion.type === 'BordersStyle'" :widgetModel="widgetModel"></WidgetBordersStyle>
                <WidgetShadowsStyle v-else-if="accordion.type === 'ShadowsStyle'" :widgetModel="widgetModel"></WidgetShadowsStyle>
                <WidgetResponsive v-else-if="accordion.type === 'Responsive'" :widgetModel="widgetModel"></WidgetResponsive>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '@/modules/documentExecution/Dashboard/Dashboard'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import descriptor from './SelectionsWidgetSettingsDescriptor.json'
import settingsTabDescriptor from '../WidgetEditorSettingsTabDescriptor.json'
import SelectorWidgetType from '../SelectorWidget/configuration/SelectorWidgetType.vue'
import SelectionsWidgetValuesManagement from './configuration/SelectionsWidgetValuesManagement.vue'
import SelectionsNoSelectionConfiguration from './configuration/SelectionsNoSelectionConfiguration.vue'
import WidgetExport from '../common/configuration/WidgetExport.vue'
import WidgetTitleStyle from '../common/style/WidgetTitleStyle.vue'
import SelectionsWidgetChipsStyle from './style/SelectionsWidgetChipsStyle.vue'
import WidgetRowsStyle from '../common/style/WidgetRowsStyle.vue'
import WidgetBackgroundColorStyle from '../common/style/WidgetBackgroundColorStyle.vue'
import WidgetPaddingStyle from '../common/style/WidgetPaddingStyle.vue'
import WidgetBordersStyle from '../common/style/WidgetBordersStyle.vue'
import WidgetShadowsStyle from '../common/style/WidgetShadowsStyle.vue'
import WidgetResponsive from '../common/responsive/WidgetResponsive.vue'
import SelectionsWidgetSettingsAccordionHeader from './SelectionsWidgetSettingsAccordionHeader.vue'

export default defineComponent({
    name: 'selections-widget-settings-container',
    components: {
        Accordion,
        AccordionTab,
        SelectorWidgetType,
        SelectionsWidgetValuesManagement,
        SelectionsNoSelectionConfiguration,
        WidgetExport,
        WidgetTitleStyle,
        SelectionsWidgetChipsStyle,
        WidgetRowsStyle,
        WidgetBackgroundColorStyle,
        WidgetPaddingStyle,
        WidgetBordersStyle,
        WidgetShadowsStyle,
        WidgetResponsive,
        SelectionsWidgetSettingsAccordionHeader
    },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        settings: { type: Array as PropType<{ title: string; type: string }[]> },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        variables: { type: Array as PropType<IVariable[]> }
    },

    data() {
        return {
            descriptor,
            settingsTabDescriptor,
            activeIndex: -1
        }
    },
    computed: {
        selectionViewMode(): string {
            return this.widgetModel.settings?.configuration?.type ?? ''
        }
    },
    watch: {
        settings() {
            this.activeIndex = -1
            this.setActiveAccordion()
        }
    },
    created() {
        this.setActiveAccordion()
    },
    methods: {
        accordionIsDisabled(accordionType: string) {
            return (accordionType === 'RowsStyle' && this.selectionViewMode === 'chips') || (accordionType === 'ChipsStyle' && this.selectionViewMode === 'list')
        },
        setActiveAccordion() {
            if (this.settings?.length === 1) this.activeIndex = 0
        }
    }
})
</script>

<style lang="scss">
.selectorAccordion {
    ::v-deep(.p-accordion-tab-active) {
        margin: 0;
    }
    .p-accordion-content {
        display: flex;
    }
}
</style>
