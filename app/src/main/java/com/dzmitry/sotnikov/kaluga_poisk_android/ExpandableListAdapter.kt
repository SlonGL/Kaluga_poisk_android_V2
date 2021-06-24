package ru.kaluga_poisk.portalkalugapoisk

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

import java.util.HashMap


class ExpandableListAdapter(
    private val context: Context, private val listDataHeader: List<MenuModel>,
    private val listDataChild: HashMap<MenuModel, List<MenuModel>>
) : BaseExpandableListAdapter() {

    override fun getChild(groupPosition: Int, childPosititon: Int): MenuModel {
        return this.listDataChild[this.listDataHeader[groupPosition]]!![childPosititon]
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildView(
        groupPosition: Int, childPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView

        val childText = getChild(groupPosition, childPosition).menuName

        if (convertView == null) {
            val infalInflater = this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = infalInflater.inflate(R.layout.list_group_child, null)
        }

        val txtListChild = convertView!!
            .findViewById(R.id.lblListItem) as TextView

        txtListChild.setText(childText)
        return convertView
    }

    override fun getChildrenCount(groupPosition: Int): Int {

        return if (this.listDataChild[this.listDataHeader[groupPosition]] == null)
            0
        else
            this.listDataChild[this.listDataHeader[groupPosition]]!!
                .size
    }

    override fun getGroup(groupPosition: Int): MenuModel {
        return this.listDataHeader[groupPosition]
    }

    override fun getGroupCount(): Int {
        return this.listDataHeader.size

    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView
        val headerTitle = getGroup(groupPosition).menuName
        if (convertView == null) {
            val infalInflater = this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = infalInflater.inflate(R.layout.list_group_header, null)
        }

        val lblListHeader = convertView!!.findViewById(R.id.lblListHeader) as TextView
        lblListHeader.setTypeface(null, Typeface.BOLD)
        lblListHeader.setText(headerTitle)

        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}