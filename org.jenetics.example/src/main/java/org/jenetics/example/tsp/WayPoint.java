/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
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
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmx.at)
 */
package org.jenetics.example.tsp;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.jenetics.util.ISeq;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
@JsonAdapter(WayPoint.Adapter.class)
public class WayPoint {

	private final String _name;
	private final Point _point;

	private WayPoint(final String name, final Point point) {
		_name = requireNonNull(name);
		_point = requireNonNull(point);
	}

	public String getName() {
		return _name;
	}

	public Point getPoint() {
		return _point;
	}

	public static WayPoint of(final String name, final Point point) {
		return new WayPoint(name, point);
	}



	static final class Adapter extends TypeAdapter<WayPoint> {

		private static final String NAME = "name";
		private static final String POINT = "point";

		@Override
		public void write(final JsonWriter out, final WayPoint point)
			throws IOException
		{
			out.beginObject();
			out.name(NAME).value(point.getName());
			out.name(POINT);
			new Point.Adapter().write(out, point.getPoint());
			out.endObject();
		}

		@Override
		public WayPoint read(final JsonReader in) throws IOException {
			in.beginObject();

			String name = null;
			Point point = null;
			switch (in.nextName()) {
				case NAME: name = in.nextString(); break;
				case POINT: point = new Point.Adapter().read(in); break;
			}

			return of(name, point);
		}
	}

	public static void main(final String[] args) throws Exception {
		for (Entry<String, List<WayPoint>> cities : points().entrySet()) {
			final File file = new File(
				"/home/fwilhelm/Temp",
				cities.getKey() + ".json"
			);

			final Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();

			final JsonWriter writer = gson.newJsonWriter(new FileWriter(file));
			writer.beginObject();
			writer.name("state").value(cities.getKey());
			writer.name("cities");
			writer.beginArray();
			for (WayPoint point : cities.getValue()) {
				new WayPoint.Adapter().write(writer, point);
			}
			writer.endArray();
			writer.endObject();
			writer.close();
		}

		final GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		final Gson gson = builder.create();
		System.out.println(gson.toJson(
			WayPoint.of("Some name", Point.of(123, 123))
		));
	}

	private static Map<String, List<WayPoint>> points() throws Exception {
		final Scanner scanner = new Scanner(Fetch.class.getResourceAsStream(
			"/org/jenetics/example/tsp/AustrianDistrictsCities.csv"
		));

		final Map<String, List<WayPoint>> pts = new HashMap<>();
		String line = scanner.nextLine();
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			System.out.println(line);

			if (!line.trim().isEmpty()) {
				final String[] parts = line.split(",");

				final String city = parts[0];
				final String state = parts[1];
				final double lat = Double.parseDouble(parts[2]);
				final double lng = Double.parseDouble(parts[3]);
				final double ele = Double.parseDouble(parts[4]);

				final List<WayPoint> way = pts
					.computeIfAbsent(state, s -> new ArrayList<>());
				way.add(WayPoint.of(city, Point.of(lat, lng, ele)));
			}
		}

		return pts;
	}

}
