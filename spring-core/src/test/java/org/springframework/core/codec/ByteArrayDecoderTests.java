/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.codec;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.junit.Test;
import reactor.core.publisher.Flux;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.MimeTypeUtils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Arjen Poutsma
 */
public class ByteArrayDecoderTests extends AbstractDecoderTestCase<ByteArrayDecoder> {

	private final byte[] fooBytes = "foo".getBytes(StandardCharsets.UTF_8);

	private final byte[] barBytes = "bar".getBytes(StandardCharsets.UTF_8);


	public ByteArrayDecoderTests() {
		super(new ByteArrayDecoder());
	}

	@Override
	@Test
	public void canDecode() {
		assertTrue(this.decoder.canDecode(ResolvableType.forClass(byte[].class),
				MimeTypeUtils.TEXT_PLAIN));
		assertFalse(this.decoder.canDecode(ResolvableType.forClass(Integer.class),
				MimeTypeUtils.TEXT_PLAIN));
		assertTrue(this.decoder.canDecode(ResolvableType.forClass(byte[].class),
				MimeTypeUtils.APPLICATION_JSON));
	}

	@Override
	@Test
	public void decode() {
		Flux<DataBuffer> input = Flux.concat(
				dataBuffer(this.fooBytes),
				dataBuffer(this.barBytes));

		testDecodeAll(input, byte[].class, step -> step
				.consumeNextWith(expectBytes(this.fooBytes))
				.consumeNextWith(expectBytes(this.barBytes))
				.verifyComplete());

	}

	@Override
	@Test
	public void decodeToMono() {
		Flux<DataBuffer> input = Flux.concat(
				dataBuffer(this.fooBytes),
				dataBuffer(this.barBytes));

		byte[] expected = new byte[this.fooBytes.length + this.barBytes.length];
		System.arraycopy(this.fooBytes, 0, expected, 0, this.fooBytes.length);
		System.arraycopy(this.barBytes, 0, expected, this.fooBytes.length, this.barBytes.length);

		testDecodeToMonoAll(input, byte[].class, step -> step
				.consumeNextWith(expectBytes(expected))
				.verifyComplete());
	}

	private Consumer<byte[]> expectBytes(byte[] expected) {
		return bytes -> assertArrayEquals(expected, bytes);
	}

}
