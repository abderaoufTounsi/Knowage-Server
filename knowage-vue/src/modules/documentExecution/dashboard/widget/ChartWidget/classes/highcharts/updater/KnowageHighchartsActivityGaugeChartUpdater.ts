import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { getFormattedLabels, getFormattedLegend, getFormattedNoDataConfiguration, getFormattedSeries } from './KnowageHighchartsCommonUpdater'

export const updateActivityGaugeChartModel = (oldModel: any, newModel: IHighchartsChartModel) => {
    getFormattedNoDataConfiguration(oldModel, newModel)
    getFormattedLegend(oldModel, newModel)
    getFormattedLabels(oldModel, newModel)
    getFormattedSeries(oldModel, newModel, 4)
    return newModel
}