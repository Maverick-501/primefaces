/*
 * The MIT License
 *
 * Copyright (c) 2009-2023 PrimeTek Informatics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primefaces.renderkit;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.component.api.Pageable;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.api.UIData;
import org.primefaces.component.api.UIPageableData;
import org.primefaces.component.paginator.CurrentPageReportRenderer;
import org.primefaces.component.paginator.FirstPageLinkRenderer;
import org.primefaces.component.paginator.JumpToPageDropdownRenderer;
import org.primefaces.component.paginator.JumpToPageInputRenderer;
import org.primefaces.component.paginator.LastPageLinkRenderer;
import org.primefaces.component.paginator.NextPageLinkRenderer;
import org.primefaces.component.paginator.PageLinksRenderer;
import org.primefaces.component.paginator.PaginatorElementRenderer;
import org.primefaces.component.paginator.PrevPageLinkRenderer;
import org.primefaces.component.paginator.RowsPerPageDropdownRenderer;
import org.primefaces.util.*;

public class DataRenderer extends CoreRenderer {

    private static final Map<String, PaginatorElementRenderer> PAGINATOR_ELEMENTS = MapBuilder.<String, PaginatorElementRenderer>builder()
            .put("{CurrentPageReport}", new CurrentPageReportRenderer())
            .put("{FirstPageLink}", new FirstPageLinkRenderer())
            .put("{PreviousPageLink}", new PrevPageLinkRenderer())
            .put("{NextPageLink}", new NextPageLinkRenderer())
            .put("{LastPageLink}", new LastPageLinkRenderer())
            .put("{PageLinks}", new PageLinksRenderer())
            .put("{RowsPerPageDropdown}", new RowsPerPageDropdownRenderer())
            .put("{JumpToPageDropdown}", new JumpToPageDropdownRenderer())
            .put("{JumpToPageInput}", new JumpToPageInputRenderer())
            .build();

    public static void addPaginatorElement(String element, PaginatorElementRenderer renderer) {
        PAGINATOR_ELEMENTS.put(element, renderer);
    }

    public static PaginatorElementRenderer removePaginatorElement(String element) {
        return PAGINATOR_ELEMENTS.remove(element);
    }

    protected void encodePaginatorMarkup(FacesContext context, Pageable pageable, String position) throws IOException {
        if (!pageable.isPaginatorAlwaysVisible() && pageable.getPageCount() <= 1) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        boolean isTop = "top".equals(position);
        UIComponent leftTopContent = pageable.getFacet("paginatorTopLeft");
        UIComponent rightTopContent = pageable.getFacet("paginatorTopRight");
        UIComponent leftBottomContent = pageable.getFacet("paginatorBottomLeft");
        UIComponent rightBottomContent = pageable.getFacet("paginatorBottomRight");

        String styleClass = isTop ? UIPageableData.PAGINATOR_TOP_CONTAINER_CLASS : UIPageableData.PAGINATOR_BOTTOM_CONTAINER_CLASS;
        String id = pageable.getClientId(context) + "_paginator_" + position;

        //add corners
        if (!isTop && pageable.getFooter() == null) {
            styleClass = styleClass + " ui-corner-bottom";
        }
        else if (isTop && pageable.getHeader() == null) {
            styleClass = styleClass + " ui-corner-top";
        }

        writer.startElement("div", null);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute("class", styleClass, null);
        writer.writeAttribute("role", "navigation", null);

        if (isTop && ComponentUtils.shouldRenderFacet(leftTopContent)) {
            writer.startElement("div", null);
            writer.writeAttribute("class", UIPageableData.PAGINATOR_TOP_LEFT_CONTENT_CLASS, null);
            renderChild(context, leftTopContent);
            writer.endElement("div");
        }

        if (isTop && ComponentUtils.shouldRenderFacet(rightTopContent)) {
            writer.startElement("div", null);
            writer.writeAttribute("class", UIPageableData.PAGINATOR_TOP_RIGHT_CONTENT_CLASS, null);
            renderChild(context, rightTopContent);
            writer.endElement("div");
        }

        String[] elements = pageable.getPaginatorTemplate().split(" ");
        for (String element : elements) {
            PaginatorElementRenderer renderer = PAGINATOR_ELEMENTS.get(element);
            if (renderer != null) {
                renderer.render(context, pageable);
            }
            else {
                if (element.startsWith("{") && element.endsWith("}")) {
                    UIComponent elementFacet = pageable.getFacet(element);
                    if (elementFacet != null) {
                        elementFacet.encodeAll(context);
                    }
                }
                else {
                    writer.write(element + " ");
                }
            }
        }
        if (!isTop && ComponentUtils.shouldRenderFacet(leftBottomContent)) {
            writer.startElement("div", null);
            writer.writeAttribute("class", UIPageableData.PAGINATOR_BOTTOM_LEFT_CONTENT_CLASS, null);
            renderChild(context, leftBottomContent);
            writer.endElement("div");
        }
        if (!isTop && ComponentUtils.shouldRenderFacet(rightBottomContent)) {
            writer.startElement("div", null);
            writer.writeAttribute("class", UIPageableData.PAGINATOR_BOTTOM_RIGHT_CONTENT_CLASS, null);
            renderChild(context, rightBottomContent);
            writer.endElement("div");
        }

        writer.endElement("div");
    }

    protected void encodePaginatorConfig(FacesContext context, Pageable pageable, WidgetBuilder wb) throws IOException {
        String clientId = pageable.getClientId(context);
        String paginatorPosition = pageable.getPaginatorPosition();
        String paginatorContainers = null;
        String currentPageTemplate = pageable.getCurrentPageReportTemplate();

        if ("both".equalsIgnoreCase(paginatorPosition)) {
            paginatorContainers = "'" + clientId + "_paginator_top','" + clientId + "_paginator_bottom'";
        }
        else {
            paginatorContainers = "'" + clientId + "_paginator_" + paginatorPosition.toLowerCase() + "'";
        }

        wb.append(",paginator:{")
                .append("id:[").append(paginatorContainers).append("]")
                .append(",rows:").append(pageable.getRows())
                .append(",rowCount:").append(pageable.getRowCount())
                .append(",page:").append(pageable.getPage());

        if (currentPageTemplate != null) {
            String currentPageTemplateTmp = currentPageTemplate.replace("'", "\\'");
            wb.append(",currentPageTemplate:'").append(currentPageTemplateTmp).append("'");
        }

        if (pageable.getPageLinks() != 10) {
            wb.append(",pageLinks:").append(pageable.getPageLinks());
        }

        if (!pageable.isPaginatorAlwaysVisible()) {
            wb.append(",alwaysVisible:false");
        }

        wb.append("}");
    }

    public void encodeFacet(FacesContext context, UIData data, String facet, String styleClass) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        UIComponent component = data.getFacet(facet);

        if (ComponentUtils.shouldRenderFacet(component)) {
            writer.startElement("div", null);
            writer.writeAttribute("class", styleClass, null);
            component.encodeAll(context);
            writer.endElement("div");
        }
    }


    protected String getHeaderLabel(FacesContext context, UIColumn column) {
        String ariaHeaderText = column.getAriaHeaderText();

        // for headerText of column
        if (ariaHeaderText == null) {
            ariaHeaderText = column.getHeaderText();
        }

        // for header facet
        if (ariaHeaderText == null) {
            UIComponent header = column.getFacet("header");
            if (ComponentUtils.shouldRenderFacet(header)) {
                if (header instanceof UIPanel) {
                    for (UIComponent child : header.getChildren()) {
                        if (child.isRendered()) {
                            String value = ComponentUtils.getValueToRender(context, child);

                            if (value != null) {
                                ariaHeaderText = value;
                                break;
                            }
                        }
                    }
                }
                else {
                    ariaHeaderText = ComponentUtils.getValueToRender(context, header);
                }
            }
        }

        return ariaHeaderText;
    }

}
