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
- **Forked from [Mekanism Energistics](https://github.com/AE2-UEL/MekanismEnergistics) and aimed at AE2UD.**
  The original is built on AE2 UEL, where every kind of content needs a storage channel and a part of its own,
  and it carried a gas copy of almost the whole mod. AE2UD has one terminal, one set of buses and one
  interface that take any registered kind of content, so all of those copies go and gas is registered as a
  kind instead.
- **The build is the same as the other AE2UD addons'.** CleanroomMC's ForgeDevEnv, the version read from the
  latest `vX.Y.Z` git tag, a build on every push and jars published to GitHub Releases on a tag. The jar is
  called `mekeng-ud`, so it cannot be mistaken for the mod it was forked from.
- **The coremod is gone.** Of its three patches, two are no longer needed - AE2UD's ME Chest opens one
  terminal for every kind of content, and its P2P registry lets a tunnel type be registered - and the third,
  which lets a Mekanism tube ask for the gas it is actually carrying, becomes a mixin.
- **Baubles and Mouse Tweaks are no longer copied into this repository.** Their API files were part of the
  source tree; they are ordinary compile-time dependencies now.
- **No version range on AE2UD yet.** AE2UD shares its mod id with AE2 and AE2 UEL, so only a version range
  tells them apart, and the range cannot be written until AE2UD's first release.
