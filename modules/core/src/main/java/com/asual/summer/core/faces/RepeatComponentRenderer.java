/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.summer.core.faces;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.apache.commons.lang.StringUtils;

import com.asual.summer.core.util.ObjectUtils;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public class RepeatComponentRenderer extends Renderer {
	
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		
		String componentTag = ComponentUtils.getComponentTag(component);
		RepeatComponent repeatComponent = ((RepeatComponent) component);
		ResponseWriter writer = context.getResponseWriter();
		
		if (componentTag != null) {
			
			writer.startElement(componentTag, component);
			
			Map<String, Object> attrs;
			
			if (ComponentUtils.getComponentClass((Component) component) == null) {
				attrs = ComponentUtils.getAttributes((Component) component, componentTag);
			} else {
				attrs = new HashMap<String, Object>();
			}
			
			List<String> classes = ComponentUtils.getComponentClasses((Component) component);
			if (classes.size() != 0) {
				attrs.put("class", StringUtils.join(classes, " "));
			}
			
			for (String key : attrs.keySet()) {
				if (ComponentUtils.shouldWriteAttribute((Component) component, key) && attrs.get(key) != null) {
					ComponentUtils.writeAttribute(writer, key, attrs.get(key));
				}
			}
			
			Object value = repeatComponent.getValue();
			if ((value == null || ObjectUtils.size(value) == 0) && repeatComponent.getDataEmpty() != null) {
				if (componentTag.matches("tbody")) {
					writer.startElement("tr", repeatComponent);
				}
				if (componentTag.matches("tbody|tr")) {
					writer.startElement("td", repeatComponent);
				}
				writer.write(repeatComponent.getDataEmpty());
				if (componentTag.matches("tbody|tr")) {
					writer.endElement("td");
				}
				if (componentTag.matches("tbody")) {
					writer.endElement("tr");
				}
			}
		}
		
		if (!StringUtils.isEmpty(repeatComponent.getDataEmptyOption())) {
			writer.startElement("option", repeatComponent);
			writer.writeAttribute("value", "", null);
			writer.write(repeatComponent.getDataEmptyOption());
			writer.endElement("option");
		}
	}
	
	public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
		if (component.getChildCount() > 0) {
			Iterator<?> itr = component.getChildren().iterator();
			while (itr.hasNext()) {
				((UIComponent) itr.next()).encodeAll(context);
			}
		}
	}

	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		String componentTag = ComponentUtils.getComponentTag(component);
		if (componentTag != null) {
			ResponseWriter writer = context.getResponseWriter();
			writer.endElement(componentTag);
		}
	}
	
	public boolean getRendersChildren() {
		return true;
	}

}