# Changelog

All notable Mekanism Energistics Unofficial Deconstructed changes are grouped by the version in which they
first appeared.

## Important compatibility notice

- Mekanism Energistics Unofficial Deconstructed runs only with AE2 Unofficial Deconstructed, not with
  standard AE2 or AE2 Unofficial Extended Life.
- Back up the world before installing or updating the mod.

## Unreleased

- **Gas is a kind of content an ME network holds.** It is registered with AE2UD the way blocks and items are
  registered with the game, so everything that was written to carry any kind of content carries gas without
  knowing what gas is: the terminals list it, the key-type picker offers it, an amount of it is typed in
  buckets or millibuckets like a fluid, and it is drawn with Mekanism's own icon and colour. A byte of a
  storage cell holds 32 buckets of gas and a machine operation moves half a bucket - the original mod's
  numbers, so nothing that was stored before the move stops fitting.
- **The buses and storage buses of AE2UD move gas.** An import bus pulls gas out of the tank it faces, an
  export bus and an interface push gas into one, and a storage bus mounts a tank as network storage, telling
  the network what changed in it rather than counting it again. None of those parts knows what gas is: each
  kind of content registers how it is moved, and this registers gas. A tank holding several gases is asked
  for the one that is wanted, instead of being left to hand over whichever it likes.
- **An AE2UD interface is a gas tank to Mekanism.** A tube or a machine next to an interface fills it and
  draws from it like any other tank. Gas put in goes to the network first and only what the network refuses
  stays in the interface; gas drawn out comes from the interface's own slots, which hold 32 buckets each, the
  same as a fluid slot. A tube of the original Mekanism or CE takes the interface's first gas, as it does from
  any tank holding several, so an interface meant for tubes should stock one gas. Under Mekanism CEu a tube
  asks for the gas it is already carrying and gets it from whichever slot holds it.
- **AE2UD's storage exposer hands out gas.** A tube or a machine next to the exposer draws any gas the
  network holds, one tank for each gas, and puts nothing in. A tube of the original Mekanism or CE takes the
  first gas, which is the one drawn from last; under Mekanism CEu a tube asks for the gas it carries.
- **A gas tank item works like a bucket.** Clicked on a gas in a terminal it fills, clicked on the terminal
  it empties into the network, and clicked on a filter slot it sets the filter to the gas it holds - or, with
  the right button, to the tank itself. One click fills or empties the whole tank, as in the original mod,
  rather than the tank's own transfer rate at a time; a creative tank gives without end.
- **A P2P tunnel carries gas.** Gas pushed into its input comes out of every output, split by how much each
  neighbour takes, as the fluid tunnel does with fluids; nothing is drawn back through it. A tunnel is attuned
  to gas with a pressurized tube of any tier, a gas tank, a jetpack, a scuba tank, a flamethrower or a gauge
  dropper. It keeps the original mod's item id, `mekeng:gas_p2p`.
- **Gas storage cells from 1k to 16384k.** A cell is AE2UD's universal storage component in a gas cell housing,
  crafted like a fluid housing with osmium in place of lapis, and is put together either from the two or in one
  shaped recipe. An empty cell comes apart in hand or alone in a crafting grid, back into its component and
  housing. Bytes per type and idle drain match AE2UD's fluid cells, and each cell holds 15 gases, as the
  original mod's did, and takes the inverter, sticky, equal distribution and void cards a fluid cell takes. The
  256k and larger cells are there only when AE2UD's high capacity storage is, like its own. The 1k to 64k cells
  keep the original mod's item ids and textures; the larger ones and the housing are drawn from AE2UD's fluid
  ones in the same colours. The original mod's gas storage components are no longer crafted.
- **Portable gas cells from 1k to 16384k.** They are AE2UD's portable cell holding gas: the same window, the
  same five types and the same share of what a storage cell of the tier holds as a portable fluid cell, the
  same cards and the same charge rate, crafted from an ME Chest, a storage component, an Energy Cell and a gas
  cell housing. A tier is there when AE2UD's portable fluid cell of that tier is. The 1k keeps the original
  mod's item id, `mekeng:portable_gas_cell`; its texture is drawn again with the others from AE2UD's portable
  fluid cells, so it now shows its tier like they do.
- **Gas works with the recipe viewer.** A gas is dragged out of HEI into any filter slot - an interface's,
  a bus's, a storage bus's - pressing the recipe key over a gas in a terminal shows its recipes, and a recipe
  that uses a gas carries it into a processing pattern.
- **Ukrainian translation.** Every name the mod shows is in Ukrainian as well as English. The Japanese and
  Chinese files keep only the names of items that still exist; the new ones show in English there.
- **Forked from [Mekanism Energistics](https://github.com/AE2-UEL/MekanismEnergistics) and aimed at AE2UD.**
  The original is built on AE2 UEL, where every kind of content needs a storage channel and a part of its own,
  and it carried a gas copy of almost the whole mod. AE2UD has one terminal, one set of buses and one
  interface that take any registered kind of content, so all of those copies go and gas is registered as a
  kind instead.
- **The build is the same as the other AE2UD addons'.** CleanroomMC's ForgeDevEnv, the version read from the
  latest `vX.Y.Z` git tag, a build on every push and jars published to GitHub Releases on a tag. The jar is
  called `mekeng-ud`, so it cannot be mistaken for the mod it was forked from.
- **Every push is compiled against all three Mekanisms.** The original, CE and CEu each get a build, so a
  change that only one of them cannot load fails there rather than in a game. One switch in
  `gradle.properties` picks which of them the game runs with during development.
- **The coremod is gone.** Of its three patches, two are no longer needed - AE2UD's ME Chest opens one
  terminal for every kind of content, and its P2P registry lets a tunnel type be registered - and the third,
  on Mekanism's tube, never changed which gas the tube got from this mod's own tanks, so it goes too.
- **Baubles and Mouse Tweaks are no longer copied into this repository.** Their API files were part of the
  source tree; they are ordinary compile-time dependencies now.
- **No version range on AE2UD yet.** AE2UD shares its mod id with AE2 and AE2 UEL, so only a version range
  tells them apart, and the range cannot be written until AE2UD's first release.
