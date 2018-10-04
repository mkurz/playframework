/*
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package play.i18n;

public interface MessagesProvider {

    public Messages messages();

    public play.api.i18n.MessagesProvider asScala();
}
