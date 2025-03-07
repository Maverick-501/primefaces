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
package org.primefaces.component.password;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.renderkit.InputRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.LangUtils;
import org.primefaces.util.WidgetBuilder;

public class PasswordRenderer extends InputRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        Password password = (Password) component;

        if (!shouldDecode(password)) {
            return;
        }

        decodeBehaviors(context, password);

        String submittedValue = context.getExternalContext().getRequestParameterMap().get(password.getClientId(context));

        if (submittedValue != null) {
            password.setSubmittedValue(submittedValue);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Password password = (Password) component;

        encodeMarkup(context, password);
        encodeScript(context, password);
    }

    protected void encodeScript(FacesContext context, Password password) throws IOException {
        boolean feedback = password.isFeedback();
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("Password", password);
        wb.attr("unmaskable", password.isToggleMask(), false);

        if (feedback) {
            wb.attr("feedback", true)
                    .attr("inline", password.isInline())
                    .attr("showEvent", password.getShowEvent(), null)
                    .attr("hideEvent", password.getHideEvent(), null)
                    .attr("promptLabel", password.getPromptLabel(), null)
                    .attr("weakLabel", password.getWeakLabel(), null)
                    .attr("goodLabel", password.getGoodLabel(), null)
                    .attr("strongLabel", password.getStrongLabel(), null);
        }

        wb.finish();
    }

    protected void encodeMarkup(FacesContext context, Password password) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = password.getClientId(context);
        boolean toggleMask = password.isToggleMask();

        if (toggleMask) {
            writer.startElement("span", null);
            boolean isRTL = ComponentUtils.isRTL(context, password);
            String positionClass = getStyleClassBuilder(context)
                        .add(Password.STYLE_CLASS)
                        .add(Password.MASKED_CLASS)
                        .add(Password.WRAPPER_CLASS)
                        .add(isRTL, "ui-input-icon-left", "ui-input-icon-right")
                        .build();
            writer.writeAttribute("class", positionClass, null);
        }

        String inputClass = getStyleClassBuilder(context)
                        .add(!toggleMask, Password.STYLE_CLASS)
                        .add(createStyleClass(password, Password.INPUT_CLASS))
                        .build();

        writer.startElement("input", password);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("name", clientId, null);
        writer.writeAttribute("type", "password", null);
        writer.writeAttribute("class", inputClass, null);
        if (password.getStyle() != null) {
            writer.writeAttribute("style", password.getStyle(), null);
        }
        if (password.isIgnoreLastPass()) {
            writer.writeAttribute("data-lpignore", "true", null);
        }

        String valueToRender = ComponentUtils.getValueToRender(context, password);
        if (LangUtils.isNotBlank(valueToRender) && password.isRedisplay()) {
            writer.writeAttribute("value", valueToRender, null);
        }

        renderAccessibilityAttributes(context, password);
        renderRTLDirection(context, password);
        renderPassThruAttributes(context, password, HTML.INPUT_TEXT_ATTRS_WITHOUT_EVENTS);
        renderDomEvents(context, password, HTML.INPUT_TEXT_EVENTS);
        renderValidationMetadata(context, password);

        writer.endElement("input");

        if (toggleMask) {
            writer.startElement("i", null);
            writer.writeAttribute("id", clientId + "_mask", "id");
            writer.writeAttribute("class", Password.ICON_CLASS, null);
            writer.endElement("i");

            writer.endElement("span");
        }
    }
}
