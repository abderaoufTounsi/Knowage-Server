<template>
    <div class="filter-content p-m-2">
        <b class="kn-truncated" v-tooltip.bottom="details?.leftOperandAlias">{{ details?.leftOperandAlias }}</b>
        <p class="kn-truncated" v-tooltip.bottom="details?.rightOperandValue">
            <b>{{ details?.operator.toLowerCase() + ' ' }}</b>
            <small>{{ details?.rightOperandValue.toLowerCase() }}</small>
        </p>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'qbe-filter-detail',
    components: {},
    props: { details: { type: Object } },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>

<style lang="scss">
.filter-content {
    max-width: 200px;
    min-width: 100px;
    height: 50px;
}
</style>
