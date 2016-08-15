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
package org.neo4j.coreedge.catchup.storecopy;

import io.netty.channel.ChannelHandlerContext;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.neo4j.coreedge.catchup.CatchupServerProtocol;
import org.neo4j.coreedge.catchup.ResponseMessageType;
import org.neo4j.coreedge.VersionCheckerChannelInboundHandler;
import org.neo4j.coreedge.identity.StoreId;
import org.neo4j.coreedge.messaging.Message;
import org.neo4j.coreedge.messaging.NetworkFlushableByteBuf;
import org.neo4j.coreedge.messaging.marsalling.storeid.StoreIdMarshal;
import org.neo4j.logging.LogProvider;

import static org.neo4j.coreedge.catchup.CatchupServerProtocol.State;

public class GetStoreIdRequestHandler extends VersionCheckerChannelInboundHandler<GetStoreIdRequest>
{
    private final CatchupServerProtocol protocol;
    private final Supplier<StoreId> storeIdSupplier;

    public GetStoreIdRequestHandler( Predicate<Message> versionChecker, CatchupServerProtocol protocol,
            Supplier<StoreId> storeIdSupplier, LogProvider logProvider )
    {
        super( versionChecker, logProvider );
        this.protocol = protocol;
        this.storeIdSupplier = storeIdSupplier;
    }

    @Override
    protected void doChannelRead0( ChannelHandlerContext ctx, GetStoreIdRequest message ) throws Exception
    {
        StoreId storeId = storeIdSupplier.get();
        ctx.writeAndFlush( ResponseMessageType.STORE_ID );
        NetworkFlushableByteBuf channel = new NetworkFlushableByteBuf( ctx.alloc().buffer() );
        channel.put( message.version() );
        StoreIdMarshal.marshal( storeId, channel );
        ctx.writeAndFlush( channel.buffer() );
        protocol.expect( State.MESSAGE_TYPE );
    }
}
