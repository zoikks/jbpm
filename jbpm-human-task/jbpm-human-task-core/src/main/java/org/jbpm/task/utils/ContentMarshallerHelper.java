/*
 * Copyright 2012 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.task.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.drools.marshalling.ObjectMarshallingStrategy;
import org.drools.marshalling.ObjectMarshallingStrategy.Context;
import org.drools.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.jbpm.task.AccessType;
import org.jbpm.task.service.ContentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentMarshallerHelper {

    private static final Logger logger = LoggerFactory.getLogger(ContentMarshallerHelper.class);

    public static ContentData marshal(Object o, ContentMarshallerContext marshallerContext, Environment env) {
        ObjectMarshallingStrategy[] strats = null;
        if (env != null && env.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES) != null) {
            strats = (ObjectMarshallingStrategy[]) env.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES);
        } else {
            strats = new ObjectMarshallingStrategy[1];
            strats[0] = new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT);
        }
        ContentData content = null;

        if (o instanceof Map) {
            for (Object key : ((Map) o).keySet()) {
                Object value = ((Map) o).get(key);
                if (value != null) {
	                MarshalledContentWrapper marshalledValue = null;
	                for (ObjectMarshallingStrategy strat : strats) {
	                    //Use the first strategy that accept the Object based on the order of the provided strategies
	                    if (strat.accept(value)) {
	                        marshalledValue = marshalSingle(strat, marshallerContext, value);
	                        break;
	                    }
	                }
	                ((Map) o).put(key, marshalledValue);
                }
            }
        }
        MarshalledContentWrapper marshalled = null;
        for (ObjectMarshallingStrategy strat : strats) {
            //Use the first strategy that accept the Object based on the order of the provided strategies
            if (strat.accept(o)) {
                marshalled = marshalSingle(strat, marshallerContext, o);
                break;
            }
        }
        content = new ContentData();
        content.setContent(marshalled.getContent());
        // A map by default should be serialized
        content.setType(marshalled.getMarshaller());
        content.setAccessType(AccessType.Inline);


        return content;
    }

    private static MarshalledContentWrapper marshalSingle(ObjectMarshallingStrategy strat, ContentMarshallerContext marshallerContext, Object o) {

        MarshalledContentWrapper contentWrap = null;
        try {
            Context context = null;
            if(marshallerContext != null){
                if (marshallerContext.strategyContext.get(strat.getClass()) == null) {
                    context = strat.createContext();
                    marshallerContext.strategyContext.put(strat.getClass(), context);
                } else {
                    context = marshallerContext.strategyContext.get(strat.getClass());
                }
            }else{
                throw new IllegalStateException(" The Marshaller Context Needs to be Provided");
            }
            byte[] marshalled = null;
            if (marshallerContext.isUseMarshal()) {
              marshalled = strat.marshal(context, null, o);
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos);
                strat.write(out, o);
                marshalled = baos.toByteArray();
                out.close();
                baos.close();
            }
            contentWrap = new MarshalledContentWrapper(marshalled, strat.getClass().getCanonicalName(), o.getClass());

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }



        return contentWrap;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object unmarshall(String type, byte[] content, ContentMarshallerContext marshallerContext, Environment env) {

        ObjectMarshallingStrategy[] strats = null;
        if (env != null && env.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES) != null) {
            strats = (ObjectMarshallingStrategy[]) env.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES);
        } else if (!marshallerContext.getStrategies().isEmpty()){

              strats = (ObjectMarshallingStrategy[]) marshallerContext.getStrategies().toArray();
        } else {
            strats = new ObjectMarshallingStrategy[1];
            strats[0] = new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT);
        }
        Object data = null;
        ObjectMarshallingStrategy selectedStrat = null;
        for (ObjectMarshallingStrategy strat : strats) {
            if (strat.getClass().getCanonicalName().equals(type)) {
                selectedStrat = strat;
            }
        }
        Context context = marshallerContext.strategyContext.get(selectedStrat.getClass());
        try {
            if (marshallerContext.isUseMarshal()) {
                data = selectedStrat.unmarshal(context, null, content, ContentMarshallerHelper.class.getClassLoader());
            } else {
                ByteArrayInputStream bs = new ByteArrayInputStream(content);
                ObjectInputStream oIn = new ObjectInputStream(bs);
                data = selectedStrat.read(oIn);
                oIn.close();
                bs.close();
            }
            if (data instanceof Map) {
                ByteArrayInputStream bs = null;
                ObjectInputStream oIn = null;
                Map localData = new HashMap();
                for (Object key : ((Map) data).keySet()) {
                	
                	Object tempVal = ((Map)data).get(key);
                	
                	if (tempVal instanceof MarshalledContentWrapper) {
                		
                		Object unmarshalledObj = null;
                	
	                    MarshalledContentWrapper value = (MarshalledContentWrapper) ((Map) data).get(key);                    
	
	                    for (ObjectMarshallingStrategy strat : strats) {
	                        if (strat.getClass().getCanonicalName().equals(value.getMarshaller())) {
	                            selectedStrat = strat;
	                        }
	                    }
	                    context = marshallerContext.strategyContext.get(selectedStrat.getClass());
	                    if (marshallerContext.isUseMarshal()) {
	                        unmarshalledObj = selectedStrat.unmarshal(context, null, value.getContent(), ContentMarshallerHelper.class.getClassLoader());
	                    } else {
	                        bs = new ByteArrayInputStream(value.getContent());
	                        oIn = new ObjectInputStream(bs);
	                        unmarshalledObj = selectedStrat.read(oIn);
	                        oIn.close();
	                        bs.close();
	                    }
	
	                    localData.put(key, unmarshalledObj);
                	}
                	else {
                		String value = (String)((Map)data).get(key);
                		localData.put(key, value);
                	}
                }
                data = localData;
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (ClassNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
        }

        return data;

    }
}
