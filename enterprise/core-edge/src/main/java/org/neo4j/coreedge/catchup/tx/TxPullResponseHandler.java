/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.coreedge.catchup.tx;

import io.netty.channel.ChannelHandlerContext;

import java.util.function.Predicate;

import org.neo4j.coreedge.catchup.CatchupClientProtocol;
import org.neo4j.coreedge.VersionCheckerChannelInboundHandler;
import org.neo4j.coreedge.messaging.Message;
import org.neo4j.logging.LogProvider;

public class TxPullResponseHandler extends VersionCheckerChannelInboundHandler<TxPullResponse>
{
    private final CatchupClientProtocol protocol;
    private final TxPullResponseListener listener;

    public TxPullResponseHandler( Predicate<Message> versionChecker, CatchupClientProtocol protocol,
            TxPullResponseListener listener, LogProvider logProvider )
    {
        super( versionChecker, logProvider );
        this.protocol = protocol;
        this.listener = listener;
    }

    @Override
    protected void doChannelRead0( ChannelHandlerContext ctx, final TxPullResponse msg ) throws Exception
    {
        if ( protocol.isExpecting( CatchupClientProtocol.State.TX_PULL_RESPONSE ) )
        {
            listener.onTxReceived( msg );
            protocol.expect( CatchupClientProtocol.State.MESSAGE_TYPE );
        }
        else
        {
            ctx.fireChannelRead( msg );
        }
    }
}
