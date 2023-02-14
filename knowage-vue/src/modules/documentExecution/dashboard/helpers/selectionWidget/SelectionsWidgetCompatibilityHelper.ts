import { IWidget, IWidgetExports } from "../../Dashboard"
import { ISelectionsWidgetNoSelections, ISelectionsWidgetSettings, ISelectionsWidgetValuesManagement, ISelectionWidgetConfiguration } from "../../interfaces/DashboardSelectionsWidget"
import { getFormattedStyle } from "./SelectionsWidgetStyleHelper"
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import * as selectionsWidgetDefaultValues from '../../widget/WidgetEditor/helpers/selectionsWidget/SelectionsWidgetDefaultValues'

export const formatSelectionWidget = (widget: any) => {
    const formattedWidget = {
        id: widget.id,
        dataset: null,
        type: widget.type,
        columns: [],
        theme: '',
        settings: {} as ISelectionsWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget) as ISelectionsWidgetSettings
    return formattedWidget
}

const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        configuration: getFormattedConfiguration(widget),
        style: getFormattedStyle(widget),
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as ISelectionsWidgetSettings
    return formattedSettings
}

const getFormattedConfiguration = (widget: any) => {
    return {
        type: widget.style?.chips?.enabled ? 'chips' : 'list',
        valuesManagement: getFormattedValuesManagement(widget),
        noSelections: getFormattedNoSelections(widget),
        exports: { showExcelExport: widget.style?.showExcelExport ?? false } as IWidgetExports
    } as ISelectionWidgetConfiguration
}


const getFormattedValuesManagement = (widget: any) => {
    if (!widget.style) return selectionsWidgetDefaultValues.getDefaultValuesManagement()
    return { showColumn: widget.style.showColumn ?? false, showDataset: widget.style.showDataset ?? false } as ISelectionsWidgetValuesManagement
}

const getFormattedNoSelections = (widget: any) => {
    return { enabled: widget.style.noSelectionsMessage, customText: widget.style.noSelectionsMessageText } as ISelectionsWidgetNoSelections
}