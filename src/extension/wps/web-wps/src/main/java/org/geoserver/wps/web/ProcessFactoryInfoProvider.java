package org.geoserver.wps.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.model.LoadableDetachableModel;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.ParamResourceModel;
import org.geoserver.wps.ProcessGroupInfo;
import org.geoserver.wps.process.GeoServerProcessors;
import org.geotools.process.ProcessFactory;
import org.geotools.process.Processors;
import org.opengis.feature.type.Name;

@SuppressWarnings("serial")
public class ProcessFactoryInfoProvider extends GeoServerDataProvider<ProcessGroupInfo> {

    private List<ProcessGroupInfo> processFactories;
    private Locale locale;

    public ProcessFactoryInfoProvider(List<ProcessGroupInfo> processFactories, Locale locale) {
        this.processFactories = processFactories;
        this.locale = locale;
    }

    @Override
    protected List<Property<ProcessGroupInfo>> getProperties() {
        List<Property<ProcessGroupInfo>> props = new ArrayList<GeoServerDataProvider.Property<ProcessGroupInfo>>();
        props.add(new BeanProperty<ProcessGroupInfo>("enabled", "enabled"));
        props.add(new AbstractProperty<ProcessGroupInfo>("prefix") {

            @Override
            public Object getPropertyValue(ProcessGroupInfo item) {
                Class factoryClass = item.getFactoryClass();
                Set<String> prefixes = new HashSet<String>();
                ProcessFactory pf = GeoServerProcessors.getProcessFactory(factoryClass, true);
                if(pf != null) {
                    Set<Name> names = pf.getNames();
                    for (Name name : names) {
                        prefixes.add(name.getNamespaceURI());
                    }
                }
                
                // if we cannot find a title use the class name
                if(prefixes.isEmpty()) {
                    return "";
                } else {
                    // build a comma separated list with the prefixes
                    List<String> pl = new ArrayList<String>(prefixes);
                    Collections.sort(pl);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < pl.size(); i++) {
                        sb.append(pl.get(i));
                        if(i < pl.size() - 1) {
                            sb.append(", ");
                        }
                    }
                    
                    return sb.toString();
                }
            }

        });
        props.add(new AbstractProperty<ProcessGroupInfo>("title") {

            @Override
            public Object getPropertyValue(ProcessGroupInfo item) {
                Class factoryClass = item.getFactoryClass();
                String title = null;
                ProcessFactory pf = GeoServerProcessors.getProcessFactory(factoryClass, true);
                if(pf != null) {
                    title = pf.getTitle().toString(locale);
                }
                
                // if we cannot find a title use the class name
                if(title == null) {
                    title = factoryClass.getName();
                }
                
                return title;
            }

        });
        props.add(new AbstractProperty<ProcessGroupInfo>("summary") {

            @Override
            public Object getPropertyValue(final ProcessGroupInfo item) {
                return new LoadableDetachableModel<String>() {

                    @Override
                    protected String load() {
                        if(item.getFilteredProcesses().isEmpty()) {
                            // all processes are enabled
                            return new ParamResourceModel("WPSAdminPage.filter.all", null).getString();
                        }
                        
                        Class factoryClass = item.getFactoryClass();
                        ProcessFactory pf = GeoServerProcessors.getProcessFactory(factoryClass, false);
                        if(pf != null) {
                            Set<Name> names = new HashSet<Name>(pf.getNames());
                            int total = names.size();
                            names.removeAll(item.getFilteredProcesses());
                            int active = names.size();
                            return new ParamResourceModel("WPSAdminPage.filter.active", null, active, total).getString();
                            
                        }
                        
                        return "?";
                    }
                };
            }

        });
        
        props.add(new PropertyPlaceholder("edit"));
        
        return props;
    }

    @Override
    protected List<ProcessGroupInfo> getItems() {
        return processFactories;
    }

}
