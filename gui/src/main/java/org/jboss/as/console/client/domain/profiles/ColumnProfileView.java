package org.jboss.as.console.client.domain.profiles;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.core.NameTokens;
import org.jboss.as.console.client.core.SuspendableViewImpl;
import org.jboss.as.console.client.domain.events.ProfileSelectionEvent;
import org.jboss.as.console.client.domain.model.ProfileRecord;
import org.jboss.as.console.client.plugins.SubsystemExtensionMetaData;
import org.jboss.as.console.client.plugins.SubsystemRegistry;
import org.jboss.as.console.client.shared.model.SubsystemRecord;
import org.jboss.as.console.client.widgets.nav.v3.ColumnManager;
import org.jboss.as.console.client.widgets.nav.v3.ContextualCommand;
import org.jboss.as.console.client.widgets.nav.v3.FinderColumn;
import org.jboss.as.console.client.widgets.nav.v3.FinderItem;
import org.jboss.as.console.client.widgets.nav.v3.MenuDelegate;
import org.jboss.ballroom.client.layout.LHSNavTreeItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Heiko Braun
 * @since 09/01/15
 */
public class ColumnProfileView extends SuspendableViewImpl
        implements ProfileMgmtPresenter.MyView {

    private final FinderColumn<ProfileRecord> profiles;
    private final FinderColumn<SubsystemLink> subsystems;
    private final FinderColumn<FinderItem> config;
    private final ArrayList<FinderItem> configLinks;
    private final ColumnManager columnManager;
    private final Widget profileColWidget;
    private final Widget subsystColWidget;
    private final Widget configColWidget;
    private final PlaceManager placeManager;
    private final LayoutPanel previewCanvas;

    private SplitLayoutPanel splitlayout;
    private LayoutPanel contentCanvas;
    private ProfileMgmtPresenter presenter;

    interface Template extends SafeHtmlTemplates {
        @Template("<div class=\"{0}\">{1}</div>")
        SafeHtml item(String cssClass, String title);
    }

    private static final Template TEMPLATE = GWT.create(Template.class);


    @Inject
    public ColumnProfileView(final PlaceManager placeManager) {
        super();
        this.placeManager = placeManager;

        contentCanvas = new LayoutPanel();
        previewCanvas = new LayoutPanel(); // TODO remove

        splitlayout = new SplitLayoutPanel(2);
        columnManager = new ColumnManager(splitlayout);

        config = new FinderColumn<FinderItem>(
                "Configuration",
                new FinderColumn.Display<FinderItem>() {

                    @Override
                    public boolean isFolder(FinderItem data) {
                        return data.isFolder();
                    }

                    @Override
                    public SafeHtml render(String baseCss, FinderItem data) {
                        return TEMPLATE.item(baseCss, data.getTitle());
                    }

                    @Override
                    public String rowCss(FinderItem data) {
                        return data.getTitle().equals("Profiles") ? "no-menu" : "";
                    }
                },
                new ProvidesKey<FinderItem>() {
                    @Override
                    public Object getKey(FinderItem item) {
                        return item.getTitle();
                    }
                });

        config.setMenuItems(new MenuDelegate<FinderItem>("View", new ContextualCommand<FinderItem>() {
            @Override
            public void executeOn(FinderItem item) {
                item.getCmd().execute();
            }
        }));

        configLinks = new ArrayList<>();

        configLinks.add(
                new FinderItem("Profiles",
                        new Command() {
                            @Override
                            public void execute() {
                                // noop
                            }
                        },
                        true
                )
        );

        configLinks.add(
                new FinderItem("Interfaces",
                        new Command() {
                            @Override
                            public void execute() {
                                placeManager.revealRelativePlace(new PlaceRequest(NameTokens.InterfacePresenter));
                            }
                        },
                        false
                )
        );

        configLinks.add(
                new FinderItem("Socket Binding",
                        new Command() {
                            @Override
                            public void execute() {
                                placeManager.revealRelativePlace(new PlaceRequest(NameTokens.SocketBindingPresenter));
                            }
                        },
                        false
                )
        );

        configLinks.add(
                new FinderItem("Paths",
                        new Command() {
                            @Override
                            public void execute() {
                                placeManager.revealRelativePlace(new PlaceRequest(NameTokens.PathManagementPresenter));
                            }
                        },
                        false
                )
        );

        configLinks.add(
                new FinderItem("System Properties",
                        new Command() {
                            @Override
                            public void execute() {
                                placeManager.revealRelativePlace(new PlaceRequest(NameTokens.PropertiesPresenter));
                            }
                        },
                        false
                )
        );

        profiles = new FinderColumn<ProfileRecord>(
                "Profiles",
                new FinderColumn.Display<ProfileRecord>() {
                    @Override
                    public boolean isFolder(ProfileRecord data) {
                        return true;
                    }

                    @Override
                    public SafeHtml render(String baseCss, ProfileRecord data) {
                        return TEMPLATE.item(baseCss, data.getName());
                    }

                    @Override
                    public String rowCss(ProfileRecord data) {
                        return "";
                    }
                },
                new ProvidesKey<ProfileRecord>() {
                    @Override
                    public Object getKey(ProfileRecord item) {
                        return item.getName();
                    }
                });

        profileColWidget = profiles.asWidget();

        subsystems = new FinderColumn<SubsystemLink>(
                "Subsystems",
                new FinderColumn.Display<SubsystemLink>() {

                    @Override
                    public boolean isFolder(SubsystemLink data) {
                        return data.isFolder();
                    }

                    @Override
                    public SafeHtml render(String baseCss, SubsystemLink data) {
                        return TEMPLATE.item(baseCss, data.getTitle());
                    }

                    @Override
                    public String rowCss(SubsystemLink data) {
                        return data.isFolder() ? "no-menu" : "";
                    }
                },
                new ProvidesKey<SubsystemLink>() {
                    @Override
                    public Object getKey(SubsystemLink item) {
                        return item.getToken();
                    }
                });

        subsystems.setMenuItems(new MenuDelegate<SubsystemLink>("View", new ContextualCommand<SubsystemLink>() {
            @Override
            public void executeOn(final SubsystemLink link) {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        placeManager.revealRelativePlace(new PlaceRequest(link.getToken()));
                    }
                });
            }
        }));

        subsystColWidget = subsystems.asWidget();

        configColWidget = config.asWidget();

        columnManager.addWest(configColWidget);
        columnManager.addWest(profileColWidget);
        columnManager.addWest(subsystColWidget);
        columnManager.add(contentCanvas);

        columnManager.setInitialVisible(1);

        // event handler

        config.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                if(config.hasSelectedItem())
                {
                    FinderItem item = config.getSelectedItem();
                    columnManager.reduceColumnsTo(1);
                    columnManager.updateActiveSelection(configColWidget);

                    clearNestedPresenter();

                    if("Profiles".equals(item.getTitle())) {

                        columnManager.appendColumn(profileColWidget);
                        presenter.loadProfiles();
                    }
                }
            }
        });

        profiles.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {

                if(profiles.hasSelectedItem()) {

                    final ProfileRecord selectedProfile = profiles.getSelectedItem();

                    clearNestedPresenter();

                    columnManager.updateActiveSelection(profileColWidget);
                    columnManager.reduceColumnsTo(2);
                    columnManager.appendColumn(subsystColWidget);

                    Scheduler.get().scheduleDeferred(
                            new Scheduler.ScheduledCommand() {
                                @Override
                                public void execute() {
                                    Console.getEventBus().fireEvent(
                                            new ProfileSelectionEvent(selectedProfile.getName())
                                    );
                                }
                            }
                    );

                }
            }
        });

        subsystems.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {

                if(subsystems.hasSelectedItem()) {

                    final SubsystemLink link = subsystems.getSelectedItem();
                    columnManager.updateActiveSelection(subsystColWidget);

                    if(link.isFolder())
                    {
                        placeManager.revealRelativePlace(new PlaceRequest(link.getToken()));
                    }
                    else
                    {
                        clearNestedPresenter();
                    }

                }
            }
        });
    }

    @Override
    public Widget createWidget() {
        Widget widget = splitlayout.asWidget();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                config.updateFrom(configLinks, true);
            }
        });
        return widget;
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {

        if (slot == ProfileMgmtPresenter.TYPE_MainContent) {
            if(content!=null)
                setContent(content);
            else
                contentCanvas.clear();
        }
    }

    private void setContent(IsWidget newContent) {
        contentCanvas.clear();
        contentCanvas.add(newContent);
    }

    @Override
    public void setProfiles(List<ProfileRecord> profileRecords) {
        profiles.updateFrom(profileRecords, false);
    }

    @Override
    public void setSubsystems(List<SubsystemRecord> subsystemRecords)
    {
        subsystems.updateFrom(matchSubsystems(subsystemRecords), false);
    }

    @Override
    public void clearActiveSelection() {
        configColWidget.getElement().removeClassName("active");
        profileColWidget.getElement().removeClassName("active");
        subsystColWidget.getElement().removeClassName("active");
    }

    private void clearNestedPresenter() {

        presenter.clearSlot(ProfileMgmtPresenter.TYPE_MainContent);

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                if(placeManager.getHierarchyDepth()>1)
                    placeManager.revealRelativePlace(1);
            }
        });
    }

    class SubsystemLink
    {
        String title;
        String token;
        private final boolean isFolder;

        public SubsystemLink(String title, String token, boolean isFolder) {
            this.title = title;
            this.token = token;
            this.isFolder = isFolder;
        }

        public String getTitle() {
            return title;
        }

        public String getToken() {
            return token;
        }

        public boolean isFolder() {
            return isFolder;
        }
    }


    private final static String[] subsystemFolders = new String[] {
            NameTokens.MailFinder
    };

    private List<SubsystemLink> matchSubsystems(List<SubsystemRecord> subsystems)
    {

        List<SubsystemLink> matches = new ArrayList<>();

        SubsystemRegistry registry = Console.getSubsystemRegistry();

        Map<String, List<SubsystemExtensionMetaData>> grouped = new HashMap<String, List<SubsystemExtensionMetaData>>();
        List<String> groupNames = new ArrayList<String>();
        for(SubsystemExtensionMetaData ext : registry.getExtensions())
        {
            if(!grouped.containsKey(ext.getGroup()))
            {
                groupNames.add(ext.getGroup());
                grouped.put(ext.getGroup(), new ArrayList<SubsystemExtensionMetaData>());
            }

            grouped.get(ext.getGroup()).add(ext);
        }

        Collections.sort(groupNames);

        // build groups first
        for(String groupName : groupNames)
        {
            List<SubsystemExtensionMetaData> items = grouped.get(groupName);

            for(SubsystemExtensionMetaData candidate : items)
            {
                for(SubsystemRecord actual: subsystems)
                {
                    if(actual.getKey().equals(candidate.getKey()))
                    {

                        final LHSNavTreeItem link = new LHSNavTreeItem(candidate.getName(), candidate.getToken());
                        link.setKey(candidate.getKey());
                        link.getElement().setAttribute("title", candidate.getName()+" "+
                                actual.getMajor()+"."+
                                actual.getMinor()+"."+
                                actual.getMicro());


                        boolean isFolder = false;
                        for (String subsystemFolder : subsystemFolders) {
                            if(candidate.getToken().equals(subsystemFolder)) {
                                isFolder = true;
                                break;
                            }
                        }

                        matches.add(
                                new SubsystemLink(candidate.getName(), candidate.getToken(), isFolder)
                        );

                    }
                }
            }

        }

        return matches;
    }

    @Override
    public void setPresenter(ProfileMgmtPresenter presenter) {

        this.presenter = presenter;
    }

    @Override
    public void setPreview(final SafeHtml html) {

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                previewCanvas.clear();
                HTML widget = new HTML(html);
                widget.getElement().setAttribute("style", "position:relative;top:100px;margin:0 auto;width:350px;overflow:hidden;padding-top:100px");
                previewCanvas.add(widget);
            }
        });

    }
}