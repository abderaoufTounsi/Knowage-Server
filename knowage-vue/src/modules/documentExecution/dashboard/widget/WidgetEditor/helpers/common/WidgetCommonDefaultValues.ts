import { IWidgetBackgroundStyle, IWidgetBordersStyle, IWidgetCrossNavigation, IWidgetLinks, IWidgetPaddingStyle, IWidgetPreview, IWidgetResponsive, IWidgetShadowsStyle, IWidgetTitle } from "@/modules/documentExecution/dashboard/Dashboard"
import descriptor from './WidgetCommonDefaultValuesDescriptor.json'
import deepcopy from 'deepcopy'

export const getDefaultResponsivnes = () => {
    return deepcopy(descriptor.defaultResponsivnes) as IWidgetResponsive
}

export const getDefaultTitleStyle = () => {
    return deepcopy(descriptor.defaultTitleStyle) as IWidgetTitle
}

export const getDefaultPaddingStyle = () => {
    return deepcopy(descriptor.defaultPaddingStyle) as IWidgetPaddingStyle
}

export const getDefaultShadowsStyle = () => {
    return deepcopy(descriptor.defaultShadowsStyle) as IWidgetShadowsStyle
}

export const getDefaultBordersStyle = () => {
    return deepcopy(descriptor.defaultBordersStyle) as IWidgetBordersStyle
}

export const getDefaultBackgroundStyle = () => {
    return deepcopy(descriptor.defaultBackgroundStyle) as IWidgetBackgroundStyle
}

export const getDefaultCrossNavigation = () => {
    return deepcopy(descriptor.defaultCrossNavigation) as IWidgetCrossNavigation
}

export const getDefaultLinks = () => {
    return deepcopy(descriptor.defaultLinks) as IWidgetLinks
}

export const getDefaultPreview = () => {
    return deepcopy(descriptor.defaultPreview) as IWidgetPreview
}