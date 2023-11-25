package me.ftmc.hotpot.blocks

import me.ftmc.hotpot.BlockPosWithLevel


class BlockPosIterator(selfPos: BlockPosWithLevel, val filter: (BlockPosWithLevel) -> Boolean) :
    Iterator<BlockPosWithLevel?> {
    private val filtered = mutableListOf<BlockPosWithLevel>()
    private var node: Node? = Node(selfPos, null)


    override fun next(): BlockPosWithLevel {
        if (!hasNext()) {
            throw NoSuchElementException()
        }
        val result: BlockPosWithLevel = node!!.getSelfPos()
        filtered.add(result)
        node = getNode(node)
        return result
    }

    override fun hasNext(): Boolean {
        return node != null
    }

    private fun getNode(node: Node?): Node? {
        var nextNode: Node
        while (node!!.hasNextNode()) {
            nextNode = node.nextNode
            val pos: BlockPosWithLevel = nextNode.getSelfPos()
            if (!filtered.contains(pos) && filter(pos)) {
                return nextNode
            }
        }
        return if (node.root == null) null else getNode(node.root)
    }

    class Node(pos: BlockPosWithLevel, root: Node?) {
        private val otherPos: Array<BlockPosWithLevel>
        private val selfPos: BlockPosWithLevel
        val root: Node?
        private var index = 0

        init {
            selfPos = pos
            this.root = root
            otherPos = arrayOf(pos.north(), pos.south(), pos.east(), pos.west())
        }

        fun hasNextNode(): Boolean {
            return index < 4
        }

        val nextNode: Node
            get() = Node(otherPos[index++], this)

        fun getSelfPos(): BlockPosWithLevel {
            return selfPos
        }
    }
}
