<template>
    <router-view
        v-show="item"
        v-slot="{ Component }"
        :functionalityId="functionalityId"
        :item="loadedItem"
        :parameterValuesMap="parameterValuesMap"
        :tabKey="key"
        @close="$emit('close', item)"
        @parametersChanged="onParametersChange"
        @iframeCreated="onIframeCreated"
        @closeIframe="$emit('closeIframe')"
        @closeDetails="$emit('close', item)"
        @documentSaved="$emit('documentSaved', $event)"
    >
        <keep-alive>
            <component :is="Component" :key="key" :functionalityId="functionalityId" :item="loadedItem" :parameterValuesMap="parameterValuesMap" :tabKey="key"></component>
        </keep-alive>
    </router-view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'document-browser-tab',
    components: {},
    emits: ['close', 'iframeCreated', 'closeIframe', 'documentSaved'],
    props: { item: { type: Object }, functionalityId: { type: String } },
    data() {
        return {
            parameterValuesMap: {} as any,
            loadedItem: null as any
        }
    },
    computed: {
        key(): string {
            return this.item?.routerId
        }
    },
    watch: {
        item() {
            this.loadItem()
        }
    },
    created() {
        this.loadItem()
    },
    methods: {
        onIframeCreated(payload: any) {
            this.$emit('iframeCreated', payload)
        },
        onParametersChange(payload: any) {
            this.parameterValuesMap[payload.document.label + '-' + this.key] = payload.parameters
        },
        loadItem() {
            this.loadedItem = this.item
        }
    }
})
</script>
