<template>
    <div class="htmlMirrorContainer" style="height: 500px; width: 100%">
        <Button icon="fas fa-ellipsis-v" class="p-button-text p-button-rounded p-button-plain editor-tags-menu-button" v-tooltip.left="$t('common.menu')" @click="toggle"></Button>
        <VCodeMirror ref="codeMirrorHtmlEditor" v-model:value="code" :options="scriptOptions" @keyup="onKeyUp" @keyDown="onKeyUp" @change="onKeyUp" @blur="onKeyUp" />
    </div>

    <TieredMenu ref="menu" :model="toolbarMenuItems" :popup="true" />
    <TagsDialog :visible="tagsDialogVisible" :widgetModel="widgetModel" :mode="tagsDialogMode" widgetType="html" :variables="variables" :selectedDatasets="selectedDatasets" :dashboardId="dashboardId" @close="closeTagsDialog" @insert="onInsert" />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IVariable, IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import VCodeMirror from 'codemirror-editor-vue3'
import TieredMenu from 'primevue/tieredmenu'
import TagsDialog from '../../common/editor/WidgetTagsDialog.vue'

export default defineComponent({
    name: 'widget-html-editor',
    components: { VCodeMirror, TieredMenu, TagsDialog },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        activeIndex: { type: Number, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        dashboardId: { type: String, required: true }
    },
    data() {
        return {
            codeMirrorHtmlEditor: null as any,
            toolbarMenuItems: [] as any[],
            tagsDialogMode: '' as string,
            tagsDialogVisible: false,
            scriptOptions: {
                cursor: true,
                line: false,
                lineNumbers: true,
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                mode: 'xml',
                tabSize: 4,
                theme: 'eclipse'
            },
            cursorPosition: null,
            code: ''
        }
    },
    watch: {
        activeIndex(value: number) {
            if (value === 1 && this.codeMirrorHtmlEditor) setTimeout(() => this.codeMirrorHtmlEditor.refresh(), 100)
        }
    },
    created() {
        this.setupCodeMirror()
    },
    methods: {
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirrorHtmlEditor) return
                this.code = this.widgetModel.settings.editor.html
                this.codeMirrorHtmlEditor = (this.$refs.codeMirrorHtmlEditor as any).cminstance as any
                setTimeout(() => {
                    this.codeMirrorHtmlEditor.refresh()
                }, 0)
                clearInterval(interval)
            }, 200)
        },
        onKeyUp() {
            this.widgetModel.settings.editor.html = this.code
        },
        toggle(event: Event) {
            this.createMenuItems()
            const menu = this.$refs.menu as any
            menu.toggle(event)
        },
        createMenuItems() {
            this.toolbarMenuItems.length = 0
            this.toolbarMenuItems.push(
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.columnsData'),
                    command: () => this.openTagsDialog('columnsData')
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.parameters'),
                    command: () => this.openTagsDialog('parameters')
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.variables'),
                    command: () => this.openTagsDialog('variables')
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.internationalization'),
                    command: () => this.openTagsDialog('internationalization')
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.repeater'),
                    items: [
                        {
                            label: this.$t('dashboard.widgetEditor.editorTags.repeater'),
                            command: () => this.openTagsDialog('repeater')
                        },
                        {
                            label: this.$t('dashboard.widgetEditor.editorTags.repeatIndex'),
                            command: () => this.openTagsDialog('repeatIndex')
                        }
                    ]
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.calculator'),
                    command: () => this.openTagsDialog('calculator')
                },
                {
                    label: this.$t('dashboard.widgetEditor.interactions.title'),
                    items: [
                        {
                            label: this.$t('dashboard.widgetEditor.editorTags.selection'),
                            command: () => this.openTagsDialog('selection')
                        },
                        {
                            label: this.$t('dashboard.widgetEditor.editorTags.preview'),
                            command: () => this.openTagsDialog('preview')
                        },
                        {
                            label: this.$t('dashboard.widgetEditor.editorTags.crossnav'),
                            command: () => this.openTagsDialog('crossnav')
                        }
                    ]
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.conditional'),
                    command: () => this.openTagsDialog('conditional')
                },
                {
                    label: this.$t('dashboard.widgetEditor.editorTags.activesel'),
                    command: () => this.openTagsDialog('activesel')
                }
            )
        },
        openTagsDialog(mode: string) {
            this.tagsDialogMode = mode
            this.tagsDialogVisible = true
        },
        closeTagsDialog() {
            this.tagsDialogVisible = false
        },
        onInsert(value: string) {
            this.cursorPosition = this.codeMirrorHtmlEditor.getCursor()
            this.codeMirrorHtmlEditor.replaceRange(value, this.cursorPosition)
            this.tagsDialogVisible = false
        }
    }
})
</script>
<style lang="scss">
.editor-tags-menu-button {
    position: absolute;
    font-size: 20px;
    top: 45px;
    right: 20px;
    z-index: 9999;
}
</style>
